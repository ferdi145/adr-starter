/**
 * 
 */
package com.salesmanager.shop.store.controller;

/**
 * Interface contain constant for Controller.These constant will be used throughout
 * sm-shop to  providing constant values to various Controllers being used in the
 * application.
 * @author Umesh A
 *
 */
public interface ControllerConstants
{

    final static String REDIRECT="redirect:";
    
    interface Tiles{
        interface ShoppingCart{
            final static String shoppingCart="maincart";
        }
        
        interface Customer{
            final static String customer="customer";
            final static String customerLogon="customerLogon";
            final static String register="register";
            final static String changePassword="customerPassword";
            final static String customerOrders="customerOrders";
            final static String customerOrder="customerOrder";
            final static String Billing="customerAddress";
            final static String EditAddress="editCustomerAddress";
        }
        
        interface Content{
            final static String content="content";
            final static String contactus="contactus";
        }
        
        interface Pages{
            final static String notFound="404";
            final static String timeout="timeout";
        }
        
        interface Merchant{
            final static String contactUs="contactus";
        }
        
        interface Checkout{
            final static String checkout="checkout";
            final static String confirmation="confirmation";
        }
        
        interface Error {
        	final static String accessDenied = "accessDenied";
        	final static String error = "error";
        }
        

        
    }

    interface Views
    {
        interface Controllers
        {
            interface Registration
            {
                String RegistrationPage = "shop/customer/registration.html";
            }
        }
    }
}
