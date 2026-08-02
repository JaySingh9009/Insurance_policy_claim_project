package com.insurance.demo.serviceImpl;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.insurance.demo.dto.IssuePolicyRequest;
import com.insurance.demo.dto.PagedResponse;
import com.insurance.demo.dto.PolicyResponse;
import com.insurance.demo.dto.PurchasePolicyRequest;
import com.insurance.demo.entity.Customer;
import com.insurance.demo.entity.Policy;
import com.insurance.demo.entity.PolicyPlan;
import com.insurance.demo.enums.PolicyStatus;
import com.insurance.demo.enums.PremiumType;
import com.insurance.demo.enums.ProductType;
import com.insurance.demo.enums.Role;
import com.insurance.demo.exception.BadRequestException;
import com.insurance.demo.exception.InvalidPolicyStatusException;
import com.insurance.demo.exception.ResourceNotFoundException;
import com.insurance.demo.exception.UnauthorizedAccessException;
import com.insurance.demo.enums.ClaimStatus;
import com.insurance.demo.repository.ClaimRepository;
import com.insurance.demo.repository.CustomerRepository;
import com.insurance.demo.repository.PolicyPlanRepository;
import com.insurance.demo.repository.PolicyRepository;
import com.insurance.demo.service.PolicyService;
import com.insurance.demo.util.NumberGenerator;
import com.insurance.demo.util.PaginationValidator;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PolicyServiceImpl implements PolicyService {

	private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "status", "startDate", "endDate");

	private final PolicyRepository policyRepository;
	private final PolicyPlanRepository planRepository;
	private final CustomerRepository customerRepository;
	private final ClaimRepository claimRepository;

	@Override
	@Transactional
	public PolicyResponse purchasePolicy(PurchasePolicyRequest request, Long userId) {

		log.info("Customer userId={} purchasing policy for planId={}", userId, request.getPlanId());

		Customer customer = customerRepository.findByUser_Id(userId).orElseThrow(
				() -> new BadRequestException("You must create a customer profile before purchasing a policy"));

		PolicyPlan plan = findActivePlan(request.getPlanId());

		ProductType productType = plan.getProduct().getProductType();

		LocalDate startDate;
		LocalDate endDate;
		PremiumType selectedType;

		if (productType == ProductType.TRAVEL) {
			// TRAVEL: customer must supply both departure date and return date
			if (request.getStartDate() == null || request.getEndDate() == null) {
				throw new BadRequestException("Travel policies require both a departure date (startDate) and a return date (endDate).");
			}
			startDate = request.getStartDate();
			endDate   = request.getEndDate();
			if (startDate.isBefore(LocalDate.now())) {
				throw new BadRequestException("Departure date cannot be in the past.");
			}
			if (!endDate.isAfter(startDate)) {
				throw new BadRequestException("Return date must be after the departure date.");
			}
			long tripDays = ChronoUnit.DAYS.between(startDate, endDate);
			if (tripDays > plan.getDuration()) {
				throw new BadRequestException(
					"Trip duration of " + tripDays + " day(s) exceeds the maximum allowed " + plan.getDuration() + " day(s) for this plan.");
			}
			// Travel policies are always ONE_TIME (single trip, fixed dates)
			selectedType = PremiumType.ONE_TIME;
		} else if (productType == ProductType.MOTOR) {
			startDate = (request.getStartDate() != null) ? request.getStartDate() : LocalDate.now();
			if (startDate.isBefore(LocalDate.now())) {
				throw new BadRequestException("Start date cannot be in the past");
			}
			endDate = startDate.plusYears(plan.getDuration());
			selectedType = plan.getPremiumType();
			if (request.getSelectedPremiumType() != null && !request.getSelectedPremiumType().isBlank()) {
				try {
					selectedType = PremiumType.valueOf(request.getSelectedPremiumType().toUpperCase());
				} catch (IllegalArgumentException ignored) {}
			}

		} else {
			// LIFE / HEALTH: existing logic unchanged
			startDate = (request.getStartDate() != null) ? request.getStartDate() : LocalDate.now();
			if (startDate.isBefore(LocalDate.now())) {
				throw new BadRequestException("Start date cannot be in the past");
			}
			endDate = startDate.plusYears(plan.getDuration());
			selectedType = plan.getPremiumType();
			if (request.getSelectedPremiumType() != null && !request.getSelectedPremiumType().isBlank()) {
				try {
					selectedType = PremiumType.valueOf(request.getSelectedPremiumType().toUpperCase());
				} catch (IllegalArgumentException ignored) {}
			}
		}

		validateNoDuplicatePolicy(customer, plan, startDate, endDate);

		double installment = calculateInstallment(plan.getPremiumAmount(), plan, selectedType);

		// ── For MOTOR: validate vehicle details and calculate IDV ────────────────
		Double motorIdvAmount = null;
		String motorVehicleRegNo = null;
		String motorVehicleMakeModel = null;
		Integer motorVehicleYear = null;
		if (productType == ProductType.MOTOR) {
			MotorVehicleData motorData = validateAndBuildMotorData(
					request.getVehicleRegistrationNo(),
					request.getVehicleMakeModel(),
					request.getVehicleYear(),
					plan.getCoverageAmount()
			);
			motorVehicleRegNo = motorData.regNo;
			motorVehicleMakeModel = motorData.makeModel;
			motorVehicleYear = motorData.year;
			motorIdvAmount = motorData.idvAmount;
		}

		Policy policy = buildPolicyEntity(customer, plan, selectedType, startDate, endDate, installment,
				motorVehicleRegNo, motorVehicleMakeModel, motorVehicleYear, motorIdvAmount);

		policy = policyRepository.save(policy);

		log.info("Policy purchased: policyId={}, policyNumber={}, selectedType={}", policy.getPolicyId(), policy.getPolicyNumber(), selectedType);

		return mapToResponse(policy);
	}

	
//	-----------------------------------ISSUEPOLICY ON BEHALF OF CUSTOMER BY OFFICER---------------------------------

	@Override
	@Transactional
	public PolicyResponse issuePolicy(IssuePolicyRequest request) {

		log.info("Admin/Agent issuing policy for customerId={}, planId={}", request.getCustomerId(),
				request.getPlanId());

		Customer customer = customerRepository.findById(request.getCustomerId()).orElseThrow(
				() -> new ResourceNotFoundException("Customer not found with ID: " + request.getCustomerId()));

		PolicyPlan plan = findActivePlan(request.getPlanId());

		ProductType productType = plan.getProduct().getProductType();

		LocalDate startDate;
		LocalDate endDate;
		PremiumType selectedType;

		if (productType == ProductType.TRAVEL) {
			// TRAVEL issued by admin/agent: departure and return dates required
			if (request.getStartDate() == null || request.getEndDate() == null) {
				throw new BadRequestException("Travel policies require both a departure date (startDate) and a return date (endDate).");
			}
			startDate = request.getStartDate();
			endDate   = request.getEndDate();
			if (startDate.isBefore(LocalDate.now())) {
				throw new BadRequestException("Departure date cannot be in the past.");
			}
			if (!endDate.isAfter(startDate)) {
				throw new BadRequestException("Return date must be after the departure date.");
			}
			long tripDays = ChronoUnit.DAYS.between(startDate, endDate);
			if (tripDays > plan.getDuration()) {
				throw new BadRequestException(
					"Trip duration of " + tripDays + " day(s) exceeds the maximum allowed " + plan.getDuration() + " day(s) for this plan.");
			}
			selectedType = PremiumType.ONE_TIME;
		} else {
			// LIFE / HEALTH / MOTOR
			startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();
			if (startDate.isBefore(LocalDate.now())) {
				throw new BadRequestException("Start date cannot be in the past");
			}
			endDate = startDate.plusYears(plan.getDuration());
			selectedType = plan.getPremiumType();
			if (request.getSelectedPremiumType() != null && !request.getSelectedPremiumType().isBlank()) {
				try {
					selectedType = PremiumType.valueOf(request.getSelectedPremiumType().toUpperCase());
				} catch (IllegalArgumentException ignored) {}
			}
		}

		validateNoDuplicatePolicy(customer, plan, startDate, endDate);

		double installment = calculateInstallment(plan.getPremiumAmount(), plan, selectedType);

		// ── For MOTOR: validate vehicle details and calculate IDV ────────────────
		Double motorIdvAmount = null;
		String motorVehicleRegNo = null;
		String motorVehicleMakeModel = null;
		Integer motorVehicleYear = null;
		if (productType == ProductType.MOTOR) {
			MotorVehicleData motorData = validateAndBuildMotorData(
					request.getVehicleRegistrationNo(),
					request.getVehicleMakeModel(),
					request.getVehicleYear(),
					plan.getCoverageAmount()
			);
			motorVehicleRegNo = motorData.regNo;
			motorVehicleMakeModel = motorData.makeModel;
			motorVehicleYear = motorData.year;
			motorIdvAmount = motorData.idvAmount;
		}

		Policy policy = buildPolicyEntity(customer, plan, selectedType, startDate, endDate, installment,
				motorVehicleRegNo, motorVehicleMakeModel, motorVehicleYear, motorIdvAmount);

		policy = policyRepository.save(policy);

		return mapToResponse(policy);
	}
	

//  ---------------------------------------------CANCEL POLICY ONLY FOR ADMIN---------------------------------------------------

	@Override
	@Transactional
	public PolicyResponse cancelPolicy(Long policyId, Long requestingUserId, String role) {

		Policy policy = findPolicy(policyId);

		if (Role.CUSTOMER.name().equalsIgnoreCase(role)) {

			Customer customer = customerRepository
					.findByUser_Id(requestingUserId)
					.orElseThrow(() ->
							new ResourceNotFoundException(
									"Customer profile not found"));

			if (!policy.getCustomer()
					.getCustomerId()
					.equals(customer.getCustomerId())) {

				throw new UnauthorizedAccessException(
						"You are not authorized to cancel this policy");
			}
		}

		if (policy.getStatus() == PolicyStatus.CANCELLED) {
			throw new InvalidPolicyStatusException("Policy is already cancelled");
		}

		if (policy.getStatus() == PolicyStatus.EXPIRED) {
			throw new InvalidPolicyStatusException("Cannot cancel an expired policy");
		}

		// Enforce no open/unresolved claims exist
		boolean hasActiveClaims = claimRepository.existsByPolicyPolicyIdAndStatusNotIn(
				policyId, List.of(ClaimStatus.APPROVED, ClaimStatus.REJECTED));
		if (hasActiveClaims) {
			throw new BadRequestException("Cannot cancel a policy that has pending or unresolved claims");
		}

		policy.setStatus(PolicyStatus.CANCELLED);
		policyRepository.save(policy);

		log.info("Policy cancelled: policyId={}", policyId);

		return mapToResponse(policy);
	}
	
	

//	----------------------------------------------------METHYOD FOR GET POLICY BY ID-------------------------------------------

	@Override
	public PolicyResponse getPolicyById(Long policyId) {

		return mapToResponse(findPolicy(policyId));
	}
	
	
//	-----------------------------------------------------GET ALL POLICY IN PAGEABLE-----------------------------------------

	@Override
	public PagedResponse<PolicyResponse> getAllPolicies(int page, int size, String sortBy, String sortDir) {

		PaginationValidator.validate(page, size, sortBy, ALLOWED_SORT_FIELDS);

		Pageable pageable = buildPageable(page, size, sortBy, sortDir);

		Page<Policy> policyPage = policyRepository.findAll(pageable);

		return toPagedResponse(policyPage);
	}
	
	
//	/-------------------------------------------GET MYPOLICIES FOR CUSTOMER---------------------------------------

	@Override
	public PagedResponse<PolicyResponse> getMyPolicies(Long userId, int page, int size, String sortBy, String sortDir) {

		PaginationValidator.validate(page, size, sortBy, ALLOWED_SORT_FIELDS);

		Customer customer = customerRepository.findByUser_Id(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));

		Pageable pageable = buildPageable(page, size, sortBy, sortDir);

		Page<Policy> policyPage = policyRepository.findByCustomerCustomerId(customer.getCustomerId(), pageable);

		return toPagedResponse(policyPage);
	}
	
	
//	-------------------------------------------------FINDACTIVE PLAN ----------------------------------------------

	private PolicyPlan findActivePlan(Long planId) {

		PolicyPlan plan = planRepository.findById(planId)
				.orElseThrow(() -> new ResourceNotFoundException("Plan not found with ID: " + planId));

		if (!plan.isActive()) {
			throw new BadRequestException("The selected plan is not active and cannot be purchased");
		}

		return plan;
	}

//	---------------------------------------------FIND POLICY---------------------------------------------
	private Policy findPolicy(Long policyId) {

		return policyRepository.findById(policyId)
				.orElseThrow(() -> new ResourceNotFoundException("Policy not found with ID: " + policyId));
	}
	
//	--------------------------------------BUILD PAGABLE----------------------------------------------

	private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {

		Sort sort = "desc".equalsIgnoreCase(sortDir) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

		return PageRequest.of(page, size, sort);
	}

	private PagedResponse<PolicyResponse> toPagedResponse(Page<Policy> policyPage) {

		List<PolicyResponse> records = policyPage.getContent().stream().map(this::mapToResponse).toList();

		return PagedResponse.<PolicyResponse>builder().records(records).currentPage(policyPage.getNumber())
				.pageSize(policyPage.getSize()).totalRecords(policyPage.getTotalElements())
				.totalPages(policyPage.getTotalPages()).isLastPage(policyPage.isLast()).build();
	}
	
	
	
//	--------------------------------------------VALIDATION CHECK FOR DUPLICATE POLICY-------------------------------

	/**
	 * Stops a customer from holding more than one active/pending/lapsed policy
	 * for the same product type (e.g. two HEALTH policies at once). LIFE
	 * products are exempt since customers may legitimately hold multiple
	 * life policies.
	 */
	private void validateNoDuplicatePolicy(Customer customer, PolicyPlan plan, LocalDate startDate, LocalDate endDate) {
		ProductType targetProductType = plan.getProduct().getProductType();

		if (targetProductType == ProductType.TRAVEL) {
			if (startDate != null && endDate != null) {
				List<Policy> overlappingTravel = policyRepository.findOverlappingTravelPolicies(
						customer.getCustomerId(),
						startDate,
						endDate,
						List.of(PolicyStatus.ACTIVE, PolicyStatus.PENDING_PAYMENT, PolicyStatus.LAPSED)
				);
				if (!overlappingTravel.isEmpty()) {
					Policy existing = overlappingTravel.get(0);
					throw new BadRequestException("Customer already has an active or pending travel policy ("
							+ existing.getPolicyNumber() + ") covering trip dates from "
							+ existing.getStartDate() + " to " + existing.getEndDate()
							+ ". Overlapping travel insurance for the same dates is not allowed.");
				}
			}
		} else if (targetProductType == ProductType.LIFE) {
			// LIFE: Customer can hold multiple different LIFE plans, but cannot buy the exact same planId twice while active/pending/lapsed
			boolean hasSameLifePlan = policyRepository.existsByCustomerCustomerIdAndPlanPlanIdAndStatusIn(
					customer.getCustomerId(),
					plan.getPlanId(),
					List.of(PolicyStatus.ACTIVE, PolicyStatus.PENDING_PAYMENT, PolicyStatus.LAPSED)
			);
			if (hasSameLifePlan) {
				throw new BadRequestException("Customer already holds an active or pending policy for the plan '"
						+ plan.getPlanName() + "'. You can purchase other Life Insurance plans, but cannot buy the exact same plan twice.");
			}
		} else if (targetProductType != ProductType.MOTOR) {
			boolean hasDuplicate = policyRepository.existsByCustomerCustomerIdAndPlanProductProductTypeAndStatusIn(
					customer.getCustomerId(),
					targetProductType,
					List.of(PolicyStatus.ACTIVE, PolicyStatus.PENDING_PAYMENT, PolicyStatus.LAPSED)
			);
			if (hasDuplicate) {
				throw new BadRequestException("Customer already has an active, pending, or lapsed policy for product type: "
						+ targetProductType + ". Only multiple LIFE and MOTOR policies are allowed.");
			}
		}
	}
	
//	---------------------------------------------CALCULATE INSTALMMENT -------------------------------------------

	private double calculateInstallment(Double totalAnnualPremium, PolicyPlan plan, PremiumType type) {
		if (totalAnnualPremium == null) return 0.0;
		if (type == PremiumType.MONTHLY) {
			return Math.round((totalAnnualPremium / 12.0) * 100.0) / 100.0;
		} else if (type == PremiumType.QUARTERLY) {
			// 1.5% discount for Quarterly frequency
			double discountedAnnual = totalAnnualPremium * 0.985;
			return Math.round((discountedAnnual / 4.0) * 100.0) / 100.0;
		} else if (type == PremiumType.SEMI_ANNUAL) {
			// 3% discount for Semi-Annual frequency
			double discountedAnnual = totalAnnualPremium * 0.97;
			return Math.round((discountedAnnual / 2.0) * 100.0) / 100.0;
		} else if (type == PremiumType.ANNUAL) {
			// 5% discount for upfront Annual payment
			double discountedAnnual = totalAnnualPremium * 0.95;
			return Math.round(discountedAnnual * 100.0) / 100.0;
		} else if (type == PremiumType.ONE_TIME) {
			if (plan != null && plan.getProduct() != null && plan.getProduct().getProductType() == ProductType.TRAVEL) {
				return Math.round(totalAnnualPremium * 100.0) / 100.0;
			}
			int duration = (plan != null && plan.getDuration() > 0) ? plan.getDuration() : 1;
			// 10% discount for full term One-Time lump-sum payment
			double discountedTotal = (totalAnnualPremium * duration) * 0.90;
			return Math.round(discountedTotal * 100.0) / 100.0;
		}
		return totalAnnualPremium;
	}
	
	
//-------------------------------------------------	CHECK METHOD FOR LAPSED POLICY----------------------------------------

	/**
	 * If an ACTIVE policy's next payment is overdue beyond the Grace Period
	 * (15 days for MONTHLY, 30 days for others), automatically flips status to LAPSED.
	 * 
	 * 
	 */
	private void checkAndSetLapsed(Policy p) {
		if (p.getStatus() == PolicyStatus.ACTIVE && p.getNextPaymentDueDate() != null) {
			int graceDays = (p.getSelectedPremiumType() == com.insurance.demo.enums.PremiumType.MONTHLY) ? 15 : 30;
			if (LocalDate.now().isAfter(p.getNextPaymentDueDate().plusDays(graceDays))) {
				p.setStatus(PolicyStatus.LAPSED);
				policyRepository.save(p);
				log.info("Policy ID {} status updated to LAPSED due to overdue payment exceeding {} days grace period", p.getPolicyId(), graceDays);
			}
		}
	}
	
//	-----------------------------------------------CALCULATE IDV FOR MOTOR TYPE----------------------------------------------

	private double calculateIDV(double baseCoverage, int vehicleAge) {
		double factor;
		if (vehicleAge < 1)      factor = 0.95;
		else if (vehicleAge < 2) factor = 0.85;
		else if (vehicleAge < 3) factor = 0.80;
		else if (vehicleAge < 4) factor = 0.70;
		else if (vehicleAge < 5) factor = 0.60;
		else if (vehicleAge < 6) factor = 0.50;
		else                     factor = 0.40; // 6-15 years
		return Math.round(baseCoverage * factor * 100.0) / 100.0;
	}
	
//----------------------------------------------POLICY RESPONSE----------------------------------------------------
	private PolicyResponse mapToResponse(Policy p) {

		String selectedType = (p.getSelectedPremiumType() != null)
				? p.getSelectedPremiumType().name()
				: (p.getPlan().getPremiumType() != null ? p.getPlan().getPremiumType().name() : "ANNUAL");

		boolean isOneTimeOrTravel = (p.getPlan() != null && p.getPlan().getProduct() != null && p.getPlan().getProduct().getProductType() == ProductType.TRAVEL)
				|| p.getSelectedPremiumType() == PremiumType.ONE_TIME
				|| "ONE_TIME".equalsIgnoreCase(selectedType);

		// Installments are only applicable for recurring payment plans (MONTHLY, QUARTERLY, etc.).
		// For TRAVEL or ONE_TIME policies, installmentAmount is null so Jackson @JsonInclude(NON_NULL) hides it.
		Double instAmount = isOneTimeOrTravel
				? null
				: ((p.getInstallmentAmount() != null)
						? p.getInstallmentAmount()
						: calculateInstallment(p.getPlan().getPremiumAmount(), p.getPlan(), p.getSelectedPremiumType() != null ? p.getSelectedPremiumType() : p.getPlan().getPremiumType()));

		String productName = (p.getPlan() != null && p.getPlan().getProduct() != null)
				? p.getPlan().getProduct().getProductName()
				: null;

		String productType = (p.getPlan() != null && p.getPlan().getProduct() != null)
				? p.getPlan().getProduct().getProductType().name()
				: null;

		Double coverageAmount = (p.getPlan() != null)
				? p.getPlan().getCoverageAmount()
				: null;

		String customerEmail = (p.getCustomer() != null && p.getCustomer().getUser() != null)
				? p.getCustomer().getUser().getEmail()
				: null;

		return PolicyResponse.builder()
				.policyId(p.getPolicyId())
				.policyNumber(p.getPolicyNumber())
				.customerId(p.getCustomer().getCustomerId())
				.customerName(p.getCustomer().getUser().getFullName())
				.customerEmail(customerEmail)
				.productName(productName)
				.planId(p.getPlan().getPlanId())
				.planName(p.getPlan().getPlanName())
				.coverageAmount(coverageAmount)
				.premiumAmount(p.getPlan().getPremiumAmount())
				.selectedPremiumType(selectedType)
				.installmentAmount(instAmount)
				.startDate(p.getStartDate())
				.endDate(p.getEndDate())
				.status(p.getStatus().name())
				.totalPremiumPaid(p.getTotalPremiumPaid())
				.lastPaymentDate(p.getLastPaymentDate())
				.nextPaymentDueDate(isOneTimeOrTravel ? null : p.getNextPaymentDueDate())
				.createdAt(p.getCreatedAt())
				// Product type for frontend detection
				.productType(productType)
				// Motor-specific fields (null for non-MOTOR policies)
				.vehicleRegistrationNo(p.getVehicleRegistrationNo())
				.vehicleMakeModel(p.getVehicleMakeModel())
				.vehicleYear(p.getVehicleYear())
				.idvAmount(p.getIdvAmount())
				.build();
	}

	private record MotorVehicleData(String regNo, String makeModel, Integer year, Double idvAmount) {}

	private MotorVehicleData validateAndBuildMotorData(String regNo, String makeModel, Integer year, double baseCoverage) {
		if (regNo == null || regNo.isBlank() || makeModel == null || makeModel.isBlank() || year == null) {
			throw new BadRequestException("Vehicle registration number, make & model, and manufacturing year are required for Motor policies.");
		}
		String cleanedRegNo = regNo.replaceAll("[\\s-]", "").toUpperCase().trim();
		if (!cleanedRegNo.matches("^[A-Z]{2}[0-9]{2}[A-Z]{1,3}[0-9]{4}$")) {
			throw new BadRequestException("Invalid vehicle registration number format: '" + regNo + "'. Expected format example: MH12AB1234");
		}

		boolean duplicateVehicle = policyRepository.existsByVehicleRegistrationNoAndStatusIn(
				cleanedRegNo,
				List.of(PolicyStatus.ACTIVE, PolicyStatus.PENDING_PAYMENT, PolicyStatus.LAPSED)
		);
		if (duplicateVehicle) {
			throw new BadRequestException("A policy already exists for vehicle registration number: " + cleanedRegNo);
		}

		int vehicleAge = LocalDate.now().getYear() - year;
		if (vehicleAge < 0 || year > LocalDate.now().getYear()) {
			throw new BadRequestException("Invalid vehicle manufacturing year: " + year);
		}
		if (vehicleAge > 15) {
			throw new BadRequestException(
					"Vehicle manufactured in " + year + " is " + vehicleAge
					+ " years old. Vehicles older than 15 years are not eligible for insurance on this platform.");
		}

		double idvAmount = calculateIDV(baseCoverage, vehicleAge);
		return new MotorVehicleData(
				cleanedRegNo,
				makeModel.trim(),
				year,
				idvAmount
		);
	}

	private Policy buildPolicyEntity(Customer customer, PolicyPlan plan, PremiumType selectedType,
									  LocalDate startDate, LocalDate endDate, double installmentAmount,
									  String motorVehicleRegNo, String motorVehicleMakeModel,
									  Integer motorVehicleYear, Double motorIdvAmount) {
		ProductType productType = plan.getProduct().getProductType();
		boolean isOneTimeOrTravel = (productType == ProductType.TRAVEL || selectedType == PremiumType.ONE_TIME);

		return Policy.builder()
				.policyNumber(NumberGenerator.generatePolicyNumber())
				.customer(customer)
				.plan(plan)
				.selectedPremiumType(selectedType)
				.installmentAmount(installmentAmount)
				.startDate(startDate)
				.endDate(endDate)
				.status(PolicyStatus.PENDING_PAYMENT)
				.totalPremiumPaid(0.0)
				.nextPaymentDueDate(isOneTimeOrTravel ? null : startDate)
				.vehicleRegistrationNo(motorVehicleRegNo)
				.vehicleMakeModel(motorVehicleMakeModel)
				.vehicleYear(motorVehicleYear)
				.idvAmount(motorIdvAmount)
				.build();
	}
}