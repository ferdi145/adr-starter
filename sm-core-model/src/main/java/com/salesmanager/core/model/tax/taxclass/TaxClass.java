package com.salesmanager.core.model.tax.taxclass;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
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

import com.salesmanager.core.integration.TransferableEntity;
import com.salesmanager.core.integration.tax.TaxClassDTO;
import org.hibernate.validator.constraints.NotEmpty;

import com.salesmanager.core.constants.SchemaConstant;
import com.salesmanager.catalog.model.product.Product;
import com.salesmanager.common.model.SalesManagerEntity;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.tax.taxrate.TaxRate;

@Entity
@Table(name = "TAX_CLASS", schema = SchemaConstant.SALESMANAGER_SCHEMA,uniqueConstraints=
    @UniqueConstraint(columnNames = {"MERCHANT_ID", "TAX_CLASS_CODE"}) )
public class TaxClass extends SalesManagerEntity<Long, TaxClass> implements TransferableEntity<TaxClassDTO> {
	private static final long serialVersionUID = -325750148480212355L;
	
	public final static String DEFAULT_TAX_CLASS = "DEFAULT";
	
	public TaxClass(String code) {
		this.code = code;
		this.title = code;
	}
	
	@Id
	@Column(name = "TAX_CLASS_ID", unique=true, nullable=false)
	@TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "TX_CLASS_SEQ_NEXT_VAL")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
	private Long id;
	
	@NotEmpty
	@Column(name="TAX_CLASS_CODE", nullable=false, length=10)
	private String code;
	
	@NotEmpty
	@Column(name = "TAX_CLASS_TITLE" , nullable=false , length=32 )
	private String title;

/*	@ManyToMany(fetch=FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name = "MERCHANT_TAXCLASS", schema=SchemaConstant.SALESMANAGER_SCHEMA, joinColumns = { 
			@JoinColumn(name = "TAX_CLASS_ID", nullable = false) }, 
			inverseJoinColumns = { @JoinColumn(name = "MERCHANT_ID", 
					nullable = false) })
	private Set<MerchantStore> stores = new HashSet<MerchantStore>();*/
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="MERCHANT_ID", nullable=true)
	private MerchantStore merchantStore;

	
	@OneToMany(mappedBy = "taxClass")
	private List<TaxRate> taxRates = new ArrayList<TaxRate>();
	
	public TaxClass() {
		super();
	}
	
	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}


	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public MerchantStore getMerchantStore() {
		return merchantStore;
	}

	public void setMerchantStore(MerchantStore merchantStore) {
		this.merchantStore = merchantStore;
	}

	@Override
	public TaxClassDTO toDTO() {
		return new TaxClassDTO(this.id, this.code, this.merchantStore.getCode());
	}
}
