package manager;

import java.util.ArrayList;

import databaseAccess.SessionDB;
import databaseAccess.TaxReturnDB;
import databaseAccess.UserDB;
import domain.TaxReturn;
import domain.User;
import exception.ConfigException;

/**
 * Class Description: Class that communicates with the TaxReturnDB class as a
 * proxy to pass information utilized in communicating with the database.
 *
 * @author Tristen Kreutz, Cesar Guzman, Jesse Goerzen
 *
 */
public final class TaxReturnManager {

	/**
	 * Get all tax returns for a user with email as a parameter
	 * 
	 * @param email the email to get returns for
	 * @return a list of tax returns matching the given email
	 * @throws ConfigException 
	 */
	public static ArrayList<TaxReturn> getByEmail(String email) throws ConfigException {

		return TaxReturnDB.getByUser(email);
	}
	
	/**
	 * Creates a new tax return in the database.
	 * @param email email of user
	 * @param year tax year
	 * @return boolean was operation successful
	 */
	public static boolean createNewTaxReturn(String email, int year) {
		String status = "i";
		TaxReturn taxReturn = new TaxReturn(email, status, year);
		
		System.out.println("AOSDFKASDKAHJSD");
		
		System.out.println(taxReturn.getStatus());
		
		if(TaxReturnDB.insert(taxReturn)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if there is a return for the given email and year.
	 * 
	 * @param email the email to check if return exists for
	 * @param taxYear the tax year to check if return exists for
	 * @return true if user has a tax return for that year, false if they do not
	 * @throws ConfigException if the config file is missing.
	 */
	public static boolean hasReturnForYear(String email, int taxYear) throws ConfigException {
		boolean hasReturn = false;
		
		ArrayList<TaxReturn> taxReturns = getByEmail(email);
		for(TaxReturn taxReturn : taxReturns) {
			if (taxReturn.getYear() == taxYear) {
				hasReturn = true;
			}
		}
		
		return hasReturn;
	}
	
	/**
	 * Makes changes to the return for the user with the given sessionID to the return from the given year and email's user.
	 * 
	 * @param email String The email of the user whose tax return is being changed.
	 * @param year int The tax return year of the tax return being changed.
	 * @param status String The status in our system of the tax return that is being updated.
	 * @param amount double The total amount that the customer is required to pay for their tax return.
	 * @param sessionID String The sessionID of the user.
	 * 		In order to update the user, the user with the given sessionID
	 * @return true if the return was updated.
	 */
	public static boolean updateReturn(String email, int year, String status, double amount, String sessionID) {
		boolean output = false;
		
		String userEmail = SessionDB.getEmail(sessionID);
		User u = null;
		if( userEmail != null && !userEmail.equals(""))
			u = UserDB.get(userEmail);
		
		if(u != null && u.getPermissionLevel() >= User.TAX_PREPARER) {
			TaxReturn tr = TaxReturnDB.get(email, year);
			if( tr != null ) {
				tr.setStatus(status);
				tr.setCost(amount);
				
				TaxReturnDB.update(tr);
				
				output = true;
			}
		}
		
		return output;
	}
}