package rs.etf.sab.student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import rs.etf.sab.operations.StockroomOperations;

public class MyStockroomOperations implements StockroomOperations {
	
	Connection connection = DB.getInstance().getConnection();
	
	@Override
	public boolean deleteStockroom(int idStockroom) {
		try (PreparedStatement statement = connection.prepareStatement("DELETE FROM Magacin WHERE idMagacin = ?");){
			statement.setInt(1, idStockroom);			
			return statement.executeUpdate() != 0;
			
		} catch (SQLException e) {
			
			return false;
		}

	}

	@Override
	public int deleteStockroomFromCity(int idCity) {
		int idStockroom = getIdStockroom(idCity);
		if (idStockroom == -1) {
			//System.out.println("here stockroom");
			return -1;
		}
		try (PreparedStatement statement = connection.prepareStatement("DELETE FROM Magacin WHERE idGrad = ?");){
			statement.setInt(1, idCity);			
			statement.executeUpdate();
			//System.out.println("or here stockroom");
			return idStockroom;
			
		} catch (SQLException e) {
			
			return 0;
		}
		
	}

	@Override
	public List<Integer> getAllStockrooms() {
		List<Integer> stockroomIds = new ArrayList<>();
		try (PreparedStatement statement = connection.prepareStatement("select idMagacin from Magacin");){
			
			try (ResultSet rs = statement.executeQuery();){
				
				while (rs.next()) {
					stockroomIds.add(rs.getInt(1));				
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
			stockroomIds = new ArrayList<>();
			return stockroomIds;
		}
		return stockroomIds;
	}

	@Override
	public int insertStockroom(int addressId) {
		int idCity = getIdCity(addressId);
		if (idCity == -1) {
			return -1;
		}
		String sql = "INSERT INTO [dbo].[Magacin] ([idMagacin] ,[idGrad]) VALUES (?,?)";
		
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, addressId);
			statement.setInt(2, idCity);
			
			int result = statement.executeUpdate();
			
			if (result != 0) {
				/*ResultSet rs = statement.getGeneratedKeys();
				if (rs.next()) {					
					return rs.getInt(1);
				}
				return -1;*/
				return addressId;
				
			} else {
				return -1;
			}	
			
			
			
		} catch (SQLException e) {
			//System.out.println("Nije uspelo insertovanje");
			//e.printStackTrace();
			return -1;
		} 
		
	}
	protected int getIdCity(int addressId) {
		int idUser = -1;
		String sql = "SELECT idGrad from [Adresa] WHERE [idAdresa] =?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, addressId);
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					idUser = rs.getInt(1);		
				}
				
			}
		} catch (SQLException e) {
			//e.printStackTrace();
		}
		
		return idUser;
	}
	protected int getIdStockroom(int idCity) {
		int idUser = -1;
		String sql = "SELECT idMagacin from [Magacin] WHERE [idGrad] =?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, idCity);
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					idUser = rs.getInt(1);		
				}
				
			}
		} catch (SQLException e) {
			//e.printStackTrace();
		}
		
		return idUser;
	}

}
