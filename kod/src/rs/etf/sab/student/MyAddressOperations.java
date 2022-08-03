package rs.etf.sab.student;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import rs.etf.sab.operations.AddressOperations;

public class MyAddressOperations implements AddressOperations {
	Connection connection = DB.getInstance().getConnection();
	
	@Override
	public int deleteAddresses(String name, int number) {
		try (PreparedStatement statement = connection.prepareStatement("DELETE FROM Adresa WHERE ulica = ? AND broj = ?");){
			statement.setString(1, name);
			statement.setInt(2, number);
			
			return statement.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public boolean deleteAdress(int idAddress) {
		try (PreparedStatement statement = connection.prepareStatement("DELETE FROM Adresa WHERE idAdresa = ?");){
			statement.setInt(1, idAddress);
			
			return statement.executeUpdate() != 0;
			
		} catch (SQLException e) {
			
			return false;
		}
		
	}

	@Override
	public int deleteAllAddressesFromCity(int idCity) {
		
		try (PreparedStatement statement = connection.prepareStatement("DELETE FROM Adresa WHERE idGrad = ?");){
			statement.setInt(1, idCity);			
			return statement.executeUpdate();
			
		} catch (SQLException e) {
			
			return 0;
		}
	}

	@Override
	public List<Integer> getAllAddresses() {
		
		List<Integer> addressIds = new ArrayList<>();
		try (PreparedStatement statement = connection.prepareStatement("SELECT idAdresa FROM Adresa");){
			
			try (ResultSet rs = statement.executeQuery();){
				
				while (rs.next()) {
					addressIds.add(rs.getInt(1));				
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
			addressIds = new ArrayList<>();
			return addressIds;
		}
		return addressIds;
	}

	@Override
	public List<Integer> getAllAddressesFromCity(int idCity) {
		if (!new MyCityOperations().getAllCities().contains(idCity)) {
			return null;
		}		
		List<Integer> addressIds = new ArrayList<>();
		try (PreparedStatement statement = connection.prepareStatement("select idAdresa from Adresa WHERE idGrad = ?");){
			statement.setInt(1, idCity);
			try (ResultSet rs = statement.executeQuery();){
				
				while (rs.next()) {
					addressIds.add(rs.getInt(1));				
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
			addressIds = new ArrayList<>();
			return addressIds;
		}
		return addressIds;
	}

	@Override
	public int insertAddress(String street, int number, int cityId, int xCord, int yCord) {
		
		String sql = "insert into Adresa ([ulica] ,[broj] ,[y] ,[idGrad] ,[x]) VALUES(?,?,?,?,?)";
		try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);){
			statement.setString(1, street);
			statement.setInt(2, number);
			statement.setInt(3, yCord);
			statement.setInt(4, cityId);
			statement.setInt(5, xCord);
			int result = statement.executeUpdate();
			
			if (result != 0) {
				ResultSet rs = statement.getGeneratedKeys();
				if (rs.next()) {					
					return rs.getInt(1);
				}
				return -1;
				
			} else {
				return -1;
			}	
			
			
			
		} catch (SQLException e) {
			//System.out.println("Nije uspelo insertovanje");
			//e.printStackTrace();
			return -1;
		} 
	}
	int getIdCityFromAddress(int idAddress) {
		int idCity = -1;
		String sql = "SELECT idGrad from [Adresa] WHERE [idAdresa] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, idAddress);
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					idCity = rs.getInt(1);		
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return idCity;
	}
	BigDecimal[] getAddressXYPosition(int idAddress) {
		BigDecimal[] ret = new BigDecimal[2];
		String sql = "SELECT x,y from [Adresa] WHERE [idAdresa] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, idAddress);
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					ret[0] = rs.getBigDecimal(1);	
					ret[1] = rs.getBigDecimal(2);	
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return ret;
	}
	BigDecimal getDistance(int idAddress1, int idAddress2) {
		BigDecimal[] pos1 = getAddressXYPosition(idAddress1);
		BigDecimal[] pos2 = getAddressXYPosition(idAddress2);
		BigDecimal deltaX = pos1[0].subtract(pos2[0]);
		BigDecimal deltaY = pos1[1].subtract(pos2[1]);
		BigDecimal ret = deltaX.pow(2).add(deltaY.pow(2));
		 //ret2 = ret.sqrt(new MathContext(4));
		Double d = Math.sqrt(ret.doubleValue());
		return BigDecimal.valueOf(d);
	}
}

