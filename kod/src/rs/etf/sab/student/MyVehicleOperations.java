package rs.etf.sab.student;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import rs.etf.sab.operations.VehicleOperations;

public class MyVehicleOperations implements VehicleOperations {
	Connection connection = DB.getInstance().getConnection();
	
	
	@Override
	public boolean changeCapacity(String licensePlateNumber, BigDecimal capacity) {
		
		String sql = "UPDATE Vozilo SET [nosivost] = ? WHERE [registracioniBroj] = ? AND (idMagacin IS NOT NULL)";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setBigDecimal(1, capacity);
			statement.setString(2, licensePlateNumber);			
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {
			
			//e.printStackTrace();
		}
		return false;

	}
	public BigDecimal getCapacity(String licensePlateNumber) {
		BigDecimal capacity = null;
		String sql = "SELECT [nosivost]  from [Vozilo] WHERE [registracioniBroj] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setString(1, licensePlateNumber);
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					capacity = rs.getBigDecimal(1);		
				}
				
			}
		} catch (SQLException e) {}
		
		return capacity;
		
	}

	@Override
	public boolean changeConsumption(String licensePlateNumber, BigDecimal fuelConsumption) {
		String sql = "UPDATE Vozilo SET [potrosnja] = ? WHERE [registracioniBroj] = ? AND (idMagacin IS NOT NULL)";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setBigDecimal(1, fuelConsumption);
			statement.setString(2, licensePlateNumber);			
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {
			
			//e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean changeFuelType(String licensePlateNumber, int fuelType) {
		System.out.println("chnage type");
		String sql = "UPDATE Vozilo SET [tipGoriva] = ? WHERE [registracioniBroj] = ? AND (idMagacin IS NOT NULL)";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, fuelType);
			statement.setString(2, licensePlateNumber);			
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
		return false;
	}
	public BigDecimal getConsumption(String licensePlateNumber) {
		BigDecimal consumption = BigDecimal.valueOf(0);
		String sql = "SELECT [potrosnja]  from [Vozilo] WHERE [registracioniBroj] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setString(1, licensePlateNumber);
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					consumption = rs.getBigDecimal(1);		
				}
				
			}
		} catch (SQLException e) {}
		
		return consumption;
		
	}
	public int getFuelType(String licensePlateNumber) {
		int fuelType = -1;
		String sql = "SELECT [tipGoriva]  from [Vozilo] WHERE [registracioniBroj] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setString(1, licensePlateNumber);
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					fuelType = rs.getInt(1);		
				}
				
			}
		} catch (SQLException e) {}
		
		return fuelType;
		
	}
	public BigDecimal getTrosak(String licensePlateNumber, BigDecimal distance) {
		int fuelType = getFuelType(licensePlateNumber);
		BigDecimal potrosnjaPoKm = getConsumption(licensePlateNumber);
		if (fuelType == -1) return null;
		
		int poKm = 0;
		if (fuelType == 0) poKm = 15;
		if (fuelType == 1) poKm = 32;
		if (fuelType == 2) poKm = 36;
		return distance.multiply(BigDecimal.valueOf(poKm)).multiply(potrosnjaPoKm);
	}

	@Override
	public int deleteVehicles(String... licencePlateNumbers) {
		int count = 0;
		for (String licencePlate: licencePlateNumbers) {			
			
			try (PreparedStatement statement = connection.prepareStatement("DELETE from Vozilo WHERE registracioniBroj = ?");){
				statement.setString(1, licencePlate);
				count += statement.executeUpdate();
			} catch (SQLException e) {
				//e.printStackTrace();				
			}

		}
		return count;
	}

	@Override
	public List<String> getAllVehichles() {
		List<String> vehicleIdList = new ArrayList<>();
		String sql = "select registracioniBroj from Vozilo";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			
			try (ResultSet rs = statement.executeQuery();){
				
				while (rs.next()) {
					vehicleIdList.add(rs.getString(1));				
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
			vehicleIdList = new ArrayList<>();
			return vehicleIdList;
		}
		//System.out.println("Broj vozila: "+vehicleIdList.size());
		return vehicleIdList;
	}

	@Override
	public boolean insertVehicle(
			String licencePlateNumber,
			int fuelType,
			BigDecimal fuelConsumtion,
			BigDecimal capacity
	) {
		String sql = "INSERT INTO [dbo].[Vozilo] "
				+ "([registracioniBroj] ,[tipGoriva] ,[potrosnja] ,[nosivost])"
				+ " VALUES (?,?,?,?)";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			
			statement.setString(1, licencePlateNumber);
			statement.setInt(2, fuelType);
			statement.setBigDecimal(3, fuelConsumtion);
			statement.setBigDecimal(4, capacity);
			
			int res = statement.executeUpdate();			
				
			return res != 0;
			
			
		} catch (SQLException e) {
			//System.out.println(e.getMessage());
			//e.printStackTrace();
			return false;
		}

	}

	@Override
	public boolean parkVehicle(String licensePlateNumber, int idStockroom) {
		if (vehicleInUse(licensePlateNumber)) {
			return false;
		}
		int idCity = new MyStockroomOperations().getIdCity(idStockroom);
		if (idCity == -1) {
			return false;
		}
		System.out.println("vozilo not in use");
		String sql = "UPDATE Vozilo SET [idMagacin] = ? WHERE [registracioniBroj] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, idStockroom);
			statement.setString(2, licensePlateNumber);			
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
		return false;
	}
	public boolean vehicleInUse(String licensePlateNumber) {
		System.out.println("vehicleInUse");
		if (licensePlateNumber == null) {
			return false;
		}
		boolean ret = false;
		String sql = "SELECT idKurir FROM VoziTrenutno WHERE [registracioniBroj] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setString(1, licensePlateNumber);	
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					ret = true;
				}
				
			}
			
		} catch (SQLException e) {
			
			//e.printStackTrace();
		}
		return ret;
	}
	public boolean removeVehicleFromUse(String vehiclePlateNumber) {
		String sql = "DELETE from VoziTrenutno WHERE registracioniBroj = ?"; 
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setString(1, vehiclePlateNumber);
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {}
		return false;
		
	}

}
