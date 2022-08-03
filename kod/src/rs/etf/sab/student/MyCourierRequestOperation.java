package rs.etf.sab.student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import rs.etf.sab.operations.CourierRequestOperation;

public class MyCourierRequestOperation implements CourierRequestOperation {
	Connection connection = DB.getInstance().getConnection();
	MyUserOperations myUserOperations = new MyUserOperations();
	
	@Override
	public boolean changeDriverLicenceNumberInCourierRequest(String userName, String licencePlateNumber) {
		int idUser = myUserOperations.getIdUser(userName);
		if (idUser == -1) {
			return false;
		}
		String sql = "UPDATE ZahtevZaKurira SET [brojVozackeDozvole] = ? WHERE [idKorisnik] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setString(1, licencePlateNumber);
			statement.setInt(2, idUser);
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean deleteCourierRequest(String userName) {
		//System.out.println("deleteCourierRequest");
		int idUser = myUserOperations.getIdUser(userName);
		if (idUser != -1) {
			return this.deleteCourierRequest(idUser);
		}
		return false;
	}

	@Override
	public List<String> getAllCourierRequests() {
		//System.out.println("getAllCourierRequests");
		String sql = "SELECT k.korisnickoIme "
				+    "FROM ZahtevZaKurira z INNER JOIN Korisnik k on(z.[idKorisnik] = k.[idKorisnik])";
		List<String> usernameList = new ArrayList<>();
		
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			try (ResultSet rs = statement.executeQuery();) {
				while (rs.next()) {
					usernameList.add(rs.getString(1));
				}
			}				
			
		} catch (SQLException e) {
			//e.printStackTrace();
		}
		
		return usernameList;
	}

	@Override
	public boolean grantRequest(String userName) {
		//System.out.println("Grant request");
		//mozda kreirati transakciju
		
		int idUser = myUserOperations.getIdUser(userName);
		if (idUser == -1) {
			return false;
		}
		//MyCourierOperations mco = new MyCourierOperations();

		//if (mco.getAllCouriers().contains(idUser)) {
			// vec je kurir
		//	return false;
		//}
		
		String driverLicenseNumber = null;
		String sql = "SELECT [brojVozackeDozvole] FROM ZahtevZaKurira WHERE idKorisnik = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, idUser);
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {


					driverLicenseNumber = rs.getString(1);		
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//System.out.println("Vozacka dozvola: " + driverLicenseNumber);
		if (driverLicenseNumber == null) {
			
			return false;
		}
		boolean result = deleteCourierRequest(idUser);
		if (result == false) {
			return false;
		}
		//System.out.println("dodje ovde");
		
		return new MyCourierOperations().insertCourier(userName, driverLicenseNumber);
		
	}

	@Override
	public boolean insertCourierRequest(String userName, String driverLicenceNumber) {
		//System.out.println("insertCourierRequest");
		int idUser = myUserOperations.getIdUser(userName);
		if (idUser == -1) {
			return false;
		}
		String sql = "INSERT INTO ZahtevZaKurira ([idKorisnik] ,[brojVozackeDozvole]) VALUES(?,?)";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, idUser);
			statement.setString(2, driverLicenceNumber);
			int result = statement.executeUpdate();			
			//System.out.println("result: " + result);	
			return result != 0;
			
			
		} catch (SQLException e) {
			//System.out.println(e.getMessage());
			//e.printStackTrace();
			return false;
		}
		
	}
	protected boolean deleteCourierRequest(int idUser) {
		try (PreparedStatement statement = connection.prepareStatement("DELETE FROM [ZahtevZaKurira] WHERE [idKorisnik] = ?");){
			statement.setInt(1, idUser);
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {
			e.printStackTrace();	
			return false;
						
		}
	}

}
