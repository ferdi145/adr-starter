package com.salesmanager.catalog.model.product.manufacturer;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;

import com.salesmanager.catalog.model.integration.core.MerchantStoreInfo;
import org.hibernate.validator.constraints.NotEmpty;

import com.salesmanager.catalog.model.common.audit.AuditListener;
import com.salesmanager.catalog.model.common.audit.AuditSection;
import com.salesmanager.catalog.model.common.audit.Auditable;
import com.salesmanager.common.model.SalesManagerEntity;
import org.jmolecules.ddd.annotation.AggregateRoot;

@AggregateRoot
@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "MANUFACTURER", uniqueConstraints=
@UniqueConstraint(columnNames = {"MERCHANT_ID", "CODE"}) )
public class Manufacturer extends SalesManagerEntity<Long, Manufacturer> implements Auditable {
	private static final long serialVersionUID = 80693964563570099L;
	
	@Id
	@Column(name = "MANUFACTURER_ID", unique=true, nullable=false)
	@TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "MANUFACT_SEQ_NEXT_VAL")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
	private Long id;
	
	@Embedded
	private AuditSection auditSection = new AuditSection();
	
	@OneToMany(mappedBy = "manufacturer", cascade = CascadeType.ALL , fetch = FetchType.EAGER)
	private Set<ManufacturerDescription> descriptions = new HashSet<ManufacturerDescription>();
	
	@Column(name = "MANUFACTURER_IMAGE")
	private String image;
	
	@Column(name="SORT_ORDER")
	private Integer order = new Integer(0);

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="MERCHANT_ID", nullable=false)
	private MerchantStoreInfo merchantStore;
	
	@NotEmpty
	@Column(name="CODE", length=100, nullable=false)
	private String code;

	public Manufacturer() {
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
	
	@Override
	public AuditSection getAuditSection() {
		return auditSection;
	}
	
	@Override
	public void setAuditSection(AuditSection auditSection) {
		this.auditSection = auditSection;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public Set<ManufacturerDescription> getDescriptions() {
		return descriptions;
	}

	public void setDescriptions(Set<ManufacturerDescription> descriptions) {
		this.descriptions = descriptions;
	}



	public MerchantStoreInfo getMerchantStore() {
		return merchantStore;
	}

	public void setMerchantStore(MerchantStoreInfo merchantStore) {
		this.merchantStore = merchantStore;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public Integer getOrder() {
		return order;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}


}
