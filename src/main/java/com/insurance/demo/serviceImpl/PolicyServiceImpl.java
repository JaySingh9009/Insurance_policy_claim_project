package com.insurance.demo.serviceImpl;

import java.time.LocalDate;
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
import com.insurance.demo.enums.ProductType;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyServiceImpl implements PolicyService {

	private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "status", "startDate", "endDate");

	private final PolicyRepository policyRepository;
	private final PolicyPlanRepository planRepository;
	private final CustomerRepository customerRepository;
	private final ClaimRepository claimRepository;

	@Override
	public PolicyResponse purchasePolicy(PurchasePolicyRequest request, Long userId) {

		log.info("Customer userId={} purchasing policy for planId={}", userId, request.getPlanId());

		Customer customer = customerRepository.findByUser_Id(userId).orElseThrow(
				() -> new BadRequestException("You must create a customer profile before purchasing a policy"));

		PolicyPlan plan = findActivePlan(request.getPlanId());

		validateNoDuplicatePolicy(customer, plan);

		LocalDate startDate = (request.getStartDate() != null) ? request.getStartDate() : LocalDate.now();
		if (startDate.isBefore(LocalDate.now())) {
			throw new BadRequestException("Start date cannot be in the past");
		}

		Policy policy = Policy.builder()
				.policyNumber(NumberGenerator.generatePolicyNumber())
				.customer(customer)
				.plan(plan)
				.startDate(startDate)
				.endDate(startDate.plusYears(plan.getDurationInYears()))
				.status(PolicyStatus.PENDING_PAYMENT) // always starts as PENDING_PAYMENT
				.totalPremiumPaid(0.0)
				.nextPaymentDueDate(startDate)
				.build();

		policy = policyRepository.save(policy);

		log.info("Policy purchased: policyId={}, policyNumber={}", policy.getPolicyId(), policy.getPolicyNumber());

		return mapToResponse(policy);
	}

	@Override
	public PolicyResponse issuePolicy(IssuePolicyRequest request) {

		log.info("Admin/Agent issuing policy for customerId={}, planId={}", request.getCustomerId(),
				request.getPlanId());

		Customer customer = customerRepository.findById(request.getCustomerId()).orElseThrow(
				() -> new ResourceNotFoundException("Customer not found with ID: " + request.getCustomerId()));

		PolicyPlan plan = findActivePlan(request.getPlanId());

		validateNoDuplicatePolicy(customer, plan);

		LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();

		Policy policy = Policy.builder()
				.policyNumber(NumberGenerator.generatePolicyNumber())
				.customer(customer)
				.plan(plan)
				.startDate(startDate)
				.endDate(startDate.plusYears(plan.getDurationInYears()))
				.status(PolicyStatus.PENDING_PAYMENT)
				.totalPremiumPaid(0.0)
				.nextPaymentDueDate(startDate)
				.build();

		policy = policyRepository.save(policy);

		return mapToResponse(policy);
	}

	@Override
	public PolicyResponse cancelPolicy(Long policyId, Long requestingUserId, String role) {

		Policy policy = findPolicy(policyId);

		if ("CUSTOMER".equals(role)) {

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

	@Override
	public PolicyResponse getPolicyById(Long policyId) {

		return mapToResponse(findPolicy(policyId));
	}

	@Override
	public PagedResponse<PolicyResponse> getAllPolicies(int page, int size, String sortBy, String sortDir) {

		PaginationValidator.validate(page, size, sortBy, ALLOWED_SORT_FIELDS);

		Pageable pageable = buildPageable(page, size, sortBy, sortDir);

		Page<Policy> policyPage = policyRepository.findAll(pageable);

		return toPagedResponse(policyPage);
	}

	@Override
	public PagedResponse<PolicyResponse> getMyPolicies(Long userId, int page, int size, String sortBy, String sortDir) {

		PaginationValidator.validate(page, size, sortBy, ALLOWED_SORT_FIELDS);

		Customer customer = customerRepository.findByUser_Id(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));

		Pageable pageable = buildPageable(page, size, sortBy, sortDir);

		Page<Policy> policyPage = policyRepository.findByCustomerCustomerId(customer.getCustomerId(), pageable);

		return toPagedResponse(policyPage);
	}

	private PolicyPlan findActivePlan(Long planId) {

		PolicyPlan plan = planRepository.findById(planId)
				.orElseThrow(() -> new ResourceNotFoundException("Plan not found with ID: " + planId));

		if (!plan.isActive()) {
			throw new BadRequestException("The selected plan is not active and cannot be purchased");
		}

		return plan;
	}

	private Policy findPolicy(Long policyId) {

		return policyRepository.findById(policyId)
				.orElseThrow(() -> new ResourceNotFoundException("Policy not found with ID: " + policyId));
	}

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

	/**
	 * Stops a customer from holding more than one active/pending/lapsed policy
	 * for the same product type (e.g. two HEALTH policies at once). LIFE
	 * products are exempt since customers may legitimately hold multiple
	 * life policies.
	 */
	private void validateNoDuplicatePolicy(Customer customer, PolicyPlan plan) {
		ProductType targetProductType = plan.getProduct().getProductType();
		if (targetProductType != ProductType.LIFE) {
			List<Policy> existingPolicies = policyRepository.findByCustomerCustomerId(customer.getCustomerId());
			boolean hasDuplicate = existingPolicies.stream().anyMatch(p ->
					p.getPlan().getProduct().getProductType() == targetProductType &&
					(p.getStatus() == PolicyStatus.ACTIVE ||
					 p.getStatus() == PolicyStatus.PENDING_PAYMENT ||
					 p.getStatus() == PolicyStatus.LAPSED)
			);
			if (hasDuplicate) {
				throw new BadRequestException("Customer already has an active, pending, or lapsed policy for product type: "
						+ targetProductType + ". Only multiple LIFE policies are allowed.");
			}
		}
	}

	/**
	 * If an ACTIVE policy's next payment is more than 30 days overdue,
	 * automatically flips it to LAPSED. Called whenever a policy is mapped
	 * to a response, so the status shown to the user is always current.
	 */
	private void checkAndSetLapsed(Policy p) {
		if (p.getStatus() == PolicyStatus.ACTIVE && p.getNextPaymentDueDate() != null) {
			if (LocalDate.now().isAfter(p.getNextPaymentDueDate().plusDays(30))) {
				p.setStatus(PolicyStatus.LAPSED);
				policyRepository.save(p);
				log.info("Policy ID {} status updated to LAPSED due to overdue payment", p.getPolicyId());
			}
		}
	}

	private PolicyResponse mapToResponse(Policy p) {

		checkAndSetLapsed(p);

		return PolicyResponse.builder()
				.policyId(p.getPolicyId())
				.policyNumber(p.getPolicyNumber())
				.customerId(p.getCustomer().getCustomerId())
				.customerName(p.getCustomer().getUser().getFullName())
				.planId(p.getPlan().getPlanId())
				.planName(p.getPlan().getPlanName())
				.startDate(p.getStartDate())
				.endDate(p.getEndDate())
				.status(p.getStatus().name())
				.totalPremiumPaid(p.getTotalPremiumPaid())
				.lastPaymentDate(p.getLastPaymentDate())
				.nextPaymentDueDate(p.getNextPaymentDueDate())
				.createdAt(p.getCreatedAt())
				.build();
	}
}