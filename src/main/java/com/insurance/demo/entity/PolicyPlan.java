package com.insurance.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "policy_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyPlan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long planId;

	private String planName;

	private Double coverageAmount;

	private Double premiumAmount;

	private Integer durationInYears;

	@Column(length = 1000)
	private String termsAndConditions;

	private boolean active;

	@ManyToOne
	@JoinColumn(name = "product_id")
	private InsuranceProduct product;
}