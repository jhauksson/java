package com.adyen.examples.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.BindingProvider;

import com.adyen.services.common.Address;
import com.adyen.services.common.Amount;
import com.adyen.services.common.BrowserInfo;
import com.adyen.services.payment.Card;
import com.adyen.services.payment.PaymentPortType;
import com.adyen.services.payment.PaymentRequest;
import com.adyen.services.payment.PaymentResult;
import com.adyen.services.payment.PaymentService;
import com.adyen.services.payment.ServiceException;

/**
 * Create 3D Secure payment (SOAP)
 *
 * 3D Secure (Verifed by VISA / MasterCard SecureCode) is an additional authentication
 * protocol that involves the shopper being redirected to their card issuer where their
 * identity is authenticated prior to the payment proceeding to an authorisation request.
 *
 * In order to start processing 3D Secure transactions, the following changes are required:
 * 1. Your Merchant Account needs to be confgured by Adyen to support 3D Secure. If you would
 *    like to have 3D Secure enabled, please submit a request to the Adyen Support Team (support@adyen.com).
 * 2. Your integration should support redirecting the shopper to the card issuer and submitting
 *    a second API call to complete the payment.
 *
 * This example demonstrates the initial API call to create the 3D secure payment using SOAP,
 * and shows the redirection the the card issuer.
 * See the API Manual for a full explanation of the steps required to process 3D Secure payments.
 *
 * @link /2.API/Soap/Create3dSecurePayment
 * @author Created by Adyen - Payments Made Easy
 */

@WebServlet(urlPatterns = { "/2.API/Soap/Create3dSecurePayment" })
public class Create3dSecurePaymentSoap extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		/**
		 * SOAP settings
		 * - wsdl: the WSDL url you are using (Test/Live)
		 * - wsUser: your web service user
		 * - wsPassword: your web service user's password
		 */
		String wsdl = "https://pal-test.adyen.com/pal/Payment.wsdl";
		String wsUser = "YourWSUser";
		String wsPassword = "YourWSUserPassword";

		/**
		 * Create SOAP client, using classes in adyen-wsdl-cxf.jar library (generated by wsdl2java tool, Apache CXF).
		 *
		 * @see WebContent/WEB-INF/lib/adyen-wsdl-cxf.jar
		 */
		PaymentService service = new PaymentService(new URL(wsdl));
		PaymentPortType client = service.getPaymentHttpPort();

		// Set HTTP Authentication
		((BindingProvider) client).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, wsUser);
		((BindingProvider) client).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, wsPassword);

		/**
		 * A payment can be submitted by sending a PaymentRequest to the authorise action of the web service.
		 * The initial API call for both 3D Secure and non-3D Secure payments is almost identical.
		 * However, for 3D Secure payments, you must supply the browserInfo object as a sub-element of the payment request.
		 * This is a container for the acceptHeader and userAgent of the shopper's browser.
		 *
		 * <pre>
		 * - merchantAccount           : The merchant account for which you want to process the payment
		 * - amount
		 *     - currency              : The three character ISO currency code.
		 *     - value                 : The transaction amount in minor units (e.g. EUR 1,00 = 100).
		 * - reference                 : Your reference for this payment.
		 * - shopperIP                 : The shopper's IP address. (recommended)
		 * - shopperEmail              : The shopper's email address. (recommended)
		 * - shopperReference          : An ID that uniquely identifes the shopper, such as a customer id. (recommended)
		 * - fraudOffset               : An integer that is added to the normal fraud score. (optional)
		 * - card
		 *     - expiryMonth           : The expiration date's month written as a 2-digit string,
		 *                               padded with 0 if required (e.g. 03 or 12).
		 *     - expiryYear            : The expiration date's year written as in full (e.g. 2016).
		 *     - holderName            : The card holder's name, as embossed on the card.
		 *     - number                : The card number.
		 *     - cvc                   : The card validation code, which is the CVC2 (MasterCard),
		 *                               CVV2 (Visa) or CID (American Express).
		 *     - billingAddress (recommended)
		 *         - street            : The street name.
		 *         - houseNumberOrName : The house number (or name).
		 *         - city              : The city.
		 *         - postalCode        : The postal/zip code.
		 *         - stateOrProvince   : The state or province.
		 *         - country           : The country in ISO 3166-1 alpha-2 format (e.g. NL).
		 * - browserInfo
		 *     - userAgent             : The user agent string of the shopper's browser (required).
		 *     - acceptHeader          : The accept header string of the shopper's browser (required).
		 * </pre>
		 */

		// Create new payment request
		PaymentRequest paymentRequest = new PaymentRequest();
		paymentRequest.setMerchantAccount("YourMerchantAccount");
		paymentRequest.setReference("TEST-3D-SECURE-PAYMENT-" + new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date()));
		paymentRequest.setShopperIP("123.123.123.123");
		paymentRequest.setShopperEmail("test@example.com");
		paymentRequest.setShopperReference("YourReference");
		paymentRequest.setFraudOffset(0);

		// Set amount
		Amount amount = new Amount();
		amount.setCurrency("EUR");
		amount.setValue(199L);
		paymentRequest.setAmount(amount);

		// Set card
		Card card = new Card();
		card.setExpiryMonth("06");
		card.setExpiryYear("2016");
		card.setHolderName("John Doe");
		card.setNumber("5212345678901234");
		card.setCvc("737");

		Address billingAddress = new Address();
		billingAddress.setStreet("Simon Carmiggeltstraat");
		billingAddress.setPostalCode("1011 DJ");
		billingAddress.setCity("Amsterdam");
		billingAddress.setHouseNumberOrName("6-50");
		billingAddress.setStateOrProvince("");
		billingAddress.setCountry("NL");
		card.setBillingAddress(billingAddress);

		paymentRequest.setCard(card);

		// Set browser info
		BrowserInfo browserInfo = new BrowserInfo();
		browserInfo.setUserAgent(request.getHeader("User-Agent"));
		browserInfo.setAcceptHeader(request.getHeader("Accept"));

		paymentRequest.setBrowserInfo(browserInfo);

		/**
		 * Send the authorise request.
		 */
		PaymentResult paymentResult;
		try {
			paymentResult = client.authorise(paymentRequest);
		} catch (ServiceException e) {
			throw new ServletException(e);
		}

		/**
		 * Once your account is configured for 3-D Secure, the Adyen system performs a directory
		 * inquiry to verify that the card is enrolled in the 3-D Secure programme.
		 * If it is not enrolled, the response is the same as a normal API authorisation.
		 * If, however, it is enrolled, the response contains these fields:
		 *
		 * - paRequest     : The 3-D request data for the issuer.
		 * - md            : The payment session.
		 * - issuerUrl     : The URL to direct the shopper to.
		 * - resultCode    : The resultCode will be RedirectShopper.
		 *
		 * The paRequest and md fields should be included in an HTML form, which needs to be submitted
		 * using the HTTP POST method to the issuerUrl. You must also include a termUrl parameter
		 * in this form, which contains the URL on your site that the shopper will be returned to
		 * by the issuer after authentication. In this example we are redirecting to another example
		 * which completes the 3D Secure payment.
		 *
		 * @see Authorise3dSecurePaymentSoap.java
		 *
		 * We recommend that the form is "self-submitting" with a fallback in case javascript is disabled.
		 * A sample form is implemented in the file below.
		 *
		 * @see WebContent/2.API/create-3d-secure-payment.jsp
		 */
		if (paymentResult.getResultCode().equals("RedirectShopper")) {
			// Set request parameters for use on the JSP page
			request.setAttribute("IssuerUrl", paymentResult.getIssuerUrl());
			request.setAttribute("PaReq", paymentResult.getPaRequest());
			request.setAttribute("MD", paymentResult.getMd());
			request.setAttribute("TermUrl", "YOUR_URL_HERE/Authorise3dSecurePayment");

			// Set correct character encoding
			response.setCharacterEncoding("UTF-8");

			// Forward request data to corresponding JSP page
			request.getRequestDispatcher("/2.API/create-3d-secure-payment.jsp").forward(request, response);
		}
		else {
			PrintWriter out = response.getWriter();

			out.println("Payment Result:");
			out.println("- pspReference: " + paymentResult.getPspReference());
			out.println("- resultCode: " + paymentResult.getResultCode());
			out.println("- authCode: " + paymentResult.getAuthCode());
			out.println("- refusalReason: " + paymentResult.getRefusalReason());
		}

	}

}
