package rs.etf.sab.student;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import rs.etf.sab.operations.CourierOperations;

public class MyCourierOperations implements CourierOperations {
	Connection connection = DB.getInstance().getConnection();
	MyUserOperations myUserOperations = new MyUserOperations();
	
	@Override
	public boolean deleteCourier(String userName) {
		int idUser = myUserOperations.getIdUser(userName);
		if (idUser == -1) {
			return false;
		}
		try (PreparedStatement statement = connection.prepareStatement("DELETE FROM [Kurir] WHERE [idKorisnik] = ?");){
			statement.setInt(1, idUser);
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {
			e.printStackTrace();	
			return false;
						
		}
	}

	@Override
	public List<String> getAllCouriers() {
		List<String> usernamesList = new ArrayList<>();
		String sql = "select ko.[korisnickoIme] from " +
				     "[Korisnik] ko INNER join Kurir ku ON (ko.idKorisnik = ku.idKurir)";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			
			try (ResultSet rs = statement.executeQuery();){
				
				while (rs.next()) {
					usernamesList.add(rs.getString(1));				
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
			usernamesList = new ArrayList<>();
			return usernamesList;
		}
		return usernamesList;
		
	}

	@Override
	public BigDecimal getAverageCourierProfit(int numberOfDeliveries) {
		String sql = "SELECT CAST(COALESCE(AVG(profit),0) as decimal(10,3)) AS ProsecanProfit "
				+    "FROM Kurir k WHERE k.brojIsporucenihPaketa = ?";
		BigDecimal average = BigDecimal.valueOf(0);
		
		
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, numberOfDeliveries);
			try (ResultSet rs = statement.executeQuery();) {
				if (rs.next()) {
					average = rs.getBigDecimal(1);
				}
			}				
			
		} catch (SQLException e) {
			//e.printStackTrace();
			
		}
		System.out.println("avg profit " + average);
		return average;
	}

	@Override
	public List<String> getCouriersWithStatus(int status) {
		List<String> usernamesList = new ArrayList<>();
		try (PreparedStatement statement = connection.prepareStatement(
				  "select ko.[korisnickoIme] from "
				+ "[Korisnik] ko INNER join Kurir ku ON (ko.idKorisnik = ku.idKurir)"
				+ "WHERE ku.[status] = ?");){
			statement.setInt(1, status);
			try (ResultSet rs = statement.executeQuery();){
				
				while (rs.next()) {
					usernamesList.add(rs.getString(1));				
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
			usernamesList = new ArrayList<>();
			return usernamesList;
		}
		return usernamesList;
	}

	@Override
	public boolean insertCourier(String userName, String driverLicenseNumber) {
		int idUser = myUserOperations.getIdUser(userName);
		if (idUser == -1) {
			return false;
		}
		
		String sql = "INSERT INTO Kurir ([idKurir] ,[brojVozackeDozvole]) VALUES(?,?)";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, idUser);
			statement.setString(2, driverLicenseNumber);

			int result = statement.executeUpdate();			
			//System.out.println("result: " + result);	
			return result != 0;
			
			
		} catch (SQLException e) {
			//System.out.println(e.getMessage());
			//e.printStackTrace();
			return false;
		}

	}
	boolean updateCourierStatus(int idCourier, int status) {
		String sql = "UPDATE Kurir SET [status] = ? WHERE [idKurir] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, status);
			statement.setInt(2, idCourier);			
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
		return false;
	}

	public boolean updateProfit(int idUser, BigDecimal profit) {
		String sql = "UPDATE Kurir SET [profit] = [profit] + ? WHERE [idKurir] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setBigDecimal(1, profit);
			statement.setInt(2, idUser);			
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
		return false;
		
	}
	public boolean incrementPackagesNumber(int idUser) {
		String sql = "UPDATE Kurir SET [brojIsporucenihPaketa] = [brojIsporucenihPaketa] + 1 "
				+    "WHERE [idKurir] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, idUser);			
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
		return false;
		
	}

}
