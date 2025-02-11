/**
 * 
 */
package com.salesmanager.catalog.presentation.populator;

import com.salesmanager.catalog.model.integration.core.LanguageInfo;
import com.salesmanager.catalog.model.integration.core.MerchantStoreInfo;
import com.salesmanager.common.business.exception.ConversionException;

import java.util.Locale;


/**
 * @author Umesh A
 *
 */
public abstract class AbstractDataPopulator<Source,Target> implements DataPopulator<Source, Target>
{

 
   
    private Locale locale;

	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	public Locale getLocale() {
		return locale;
	}
	

	@Override
	public Target populate(Source source, MerchantStoreInfo store, LanguageInfo language) throws ConversionException{
	   return populate(source,createTarget(), store, language);
	}
	
	protected abstract Target createTarget();

   

}
