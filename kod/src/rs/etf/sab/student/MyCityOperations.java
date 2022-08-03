package rs.etf.sab.student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import rs.etf.sab.operations.CityOperations;

public class MyCityOperations implements CityOperations {
	Connection connection = DB.getInstance().getConnection();
	@Override
	public int deleteCity(String... names) {
		//System.out.println("delete city names");
		
		int count = 0;
		for (String name: names) {
			try (PreparedStatement statement = connection.prepareStatement("DELETE FROM Grad WHERE naziv = ?");){
				statement.setString(1, name);
				count += statement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
				
			}
			
			
		}
		
		return count;
	}

	@Override
	public boolean deleteCity(int idCity) {
		//System.out.println("delete city idcity");
		try (PreparedStatement statement = connection.prepareStatement("delete from Grad WHERE idGrad = ?");){
			statement.setInt(1, idCity);
			
			return statement.executeUpdate() != 0;
			
		} catch (SQLException e) {
			
			return false;
		}
		
	}

	@Override
	public List<Integer> getAllCities() {
		//System.out.println("select city ids");
		List<Integer> cityIds = new ArrayList<>();
		try (PreparedStatement statement = connection.prepareStatement("select idGrad from Grad");){
			
			try (ResultSet rs = statement.executeQuery();){
				
				while (rs.next()) {
					cityIds.add(rs.getInt(1));				
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
			cityIds = new ArrayList<>();
			return cityIds;
		}
		//System.out.println("Velicina: " + cityIds.size());
		return cityIds;
	}

	@Override
	public int insertCity(String name, String postalCode) {
		String sql = "insert into Grad ([naziv] ,[postanskiBroj]) VALUES(?,?)";
		try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);){
			statement.setString(1, name);
			statement.setString(2, postalCode);
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

}
