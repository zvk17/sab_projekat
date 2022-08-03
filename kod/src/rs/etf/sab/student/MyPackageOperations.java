package rs.etf.sab.student;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import rs.etf.sab.operations.PackageOperations;

public class MyPackageOperations implements PackageOperations {
	Connection connection = DB.getInstance().getConnection();
	MyUserOperations myUserOperations = new MyUserOperations();
	MyAddressOperations myAddressOperations = new MyAddressOperations();
	
	@Override
	public boolean acceptAnOffer(int packageId) {
		String sql = "UPDATE Isporuka SET [status] = 1, [vremePrihvatanja] = GETDATE() "
				+ " WHERE [idIsporuka] = ? AND [status] = 0";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, packageId);			
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {
			
			//e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean changeType(int packageId, int newType) {
		String sql = "UPDATE Isporuka SET [tipPaketa] = ? WHERE [idIsporuka] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, newType);	
			statement.setInt(2, packageId);		
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {
			
			//e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean changeWeight(int packageId, BigDecimal newWeight) {
		String sql = "UPDATE Isporuka SET [tezinaPaketa] = ? WHERE [idIsporuka] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setBigDecimal(1, newWeight);	
			statement.setInt(2, packageId);		
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {
			
			//e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean deletePackage(int packageId) {
		String sql = "DELETE FROM [Isporuka] WHERE [idIsporuka] = ? AND [status] IN (0,4)";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, packageId);
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {
			e.printStackTrace();	
			return false;
						
		}
	}

	@Override
	public Date getAcceptanceTime(int packageId) {
		Date acceptanceTime = null;
		String sql = "SELECT [vremePrihvatanja] FROM  [Isporuka] WHERE [idIsporuka] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, packageId);	
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					acceptanceTime = rs.getDate(1);		
				}
				
			}
			
		} catch (SQLException e) {
			
			//e.printStackTrace();
		}
		return acceptanceTime;
	}

	@Override
	public List<Integer> getAllPackages() {
		List<Integer> packageIdsList = new ArrayList<>();
		try (PreparedStatement statement = connection.prepareStatement("SELECT idIsporuka FROM Isporuka");){
			
			try (ResultSet rs = statement.executeQuery();){
				
				while (rs.next()) {
					packageIdsList.add(rs.getInt(1));				
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
			packageIdsList = new ArrayList<>();
			return packageIdsList;
		}
		return packageIdsList;
	}

	@Override
	public List<Integer> getAllPackagesCurrentlyAtCity(int idCity) {

		List<Integer> packageList = this.getAllPackages();
		
		return this.filterPackagesByCity(packageList, idCity);
		
	}

	@Override
	public List<Integer> getAllPackagesWithSpecificType(int type) {
		List<Integer> specificTypePackagesList = new ArrayList<>();
		String sql = "SELECT idIsporuka FROM Isporuka WHERE [tipPaketa] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, type);		
			try (ResultSet rs = statement.executeQuery();){
				
				while (rs.next()) {
					specificTypePackagesList.add(rs.getInt(1));				
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
			specificTypePackagesList = new ArrayList<>();
			return specificTypePackagesList;
		}
		return specificTypePackagesList;
	}

	@Override
	public List<Integer> getAllUndeliveredPackages() {
		List<Integer> undeliveredPackageIdsList = new ArrayList<>();
		String sql = "SELECT idIsporuka FROM Isporuka WHERE status IN (1,2)";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			
			try (ResultSet rs = statement.executeQuery();){
				
				while (rs.next()) {
					undeliveredPackageIdsList.add(rs.getInt(1));				
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
			undeliveredPackageIdsList = new ArrayList<>();
			return undeliveredPackageIdsList;
		}
		return undeliveredPackageIdsList;
	}

	@Override
	public List<Integer> getAllUndeliveredPackagesFromCity(int idCity) {
		
		List<Integer> packageList = new ArrayList<Integer>();
		String sql = "Select i.idIsporuka from "
				   + "Isporuka i INNER JOIN Adresa a on (i.idPolaznaAdresa = a.idAdresa) "
				   + "WHERE a.idGrad = ? AND "
				   + "i.status IN (1, 2)";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, idCity);
			try (ResultSet rs = statement.executeQuery();){
				
				while (rs.next()) {
					packageList.add(rs.getInt(1));				
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
			packageList = new ArrayList<>();
			return packageList;
		}
		
		return packageList;
		
	}

	private List<Integer> filterPackagesByCity(List<Integer> packageList, int idCity) {
		List<Integer> retList = new ArrayList<>();
		for (int packageId: packageList) {
			int calcCity = this.getCurrentLocationOfPackage(packageId);
			if (calcCity == idCity) {
				retList.add(packageId);
			}
		}
		return retList;
	}

	@Override
	public int getCurrentLocationOfPackage(int packageId) {
		int cityId = -1;
		int packageStatus = getDeliveryStatus(packageId);
		
		Integer[] addresses = this.getAddressId(packageId);
		if (addresses == null) {
			 return -1;
		}
		if (packageStatus != 2) {
				
			if (packageStatus == 0 || packageStatus == 1 || packageStatus == 4) {
				//pocetna lokacija
				cityId = this.myAddressOperations.getIdCityFromAddress(addresses[0]);
			} else {
				//konacna lokacija
				cityId = this.myAddressOperations.getIdCityFromAddress(addresses[1]);
			}
			
		} else {
			//u tranzitu provera magacina
			if (addresses[2] != null) {
				cityId = this.myAddressOperations.getIdCityFromAddress(addresses[2]);
			}
		}

		
		return cityId;
	}

	@Override
	public int getDeliveryStatus(int packageId) {
		int status = -1;
		String sql = "SELECT [status] FROM  [Isporuka] WHERE [idIsporuka] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, packageId);	
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					status = rs.getInt(1);		
				}
				
			}
			
		} catch (SQLException e) {
			
			//e.printStackTrace();
		}
		return status;
	}
	public boolean updateDeliveryStatus(int packageId, int newStatus) {
		String sql = "UPDATE Isporuka SET [status] = ? WHERE [idIsporuka] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, newStatus);	
			statement.setInt(2, packageId);		
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {
			
			//e.printStackTrace();
		}
		return false;
	}

	@Override
	public BigDecimal getPriceOfDelivery(int packageId) {
		BigDecimal price = null;
		String sql = "SELECT [cena] FROM  [Isporuka] WHERE [idIsporuka] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, packageId);	
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					price = rs.getBigDecimal(1);		
				}
				
			}
			
		} catch (SQLException e) {
			
			//e.printStackTrace();
		}
		return price;
	}
	public BigDecimal getWeightOfPackage(int idPackage) {
		BigDecimal weight = null;
		String sql = "SELECT [tezinaPaketa] FROM  [Isporuka] WHERE [idIsporuka] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, idPackage);	
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					weight = rs.getBigDecimal(1);		
				}
				
			}
			
		} catch (SQLException e) {
			
			//e.printStackTrace();
		}
		return weight;
	}
	@Override
	public int insertPackage(
			int addressFrom,
			int addressTo,
			String userName,
			int packageType,
			BigDecimal weight
	) {
		int idUser = myUserOperations.getIdUser(userName);
		if (idUser == -1) {
			return -1;
		}
		System.out.println("inserting package");
		
		String sql = "INSERT INTO [dbo].[Isporuka] "
				+ "([idPolaznaAdresa] ,[idDolaznaAdresa] ,[tipPaketa] ,[tezinaPaketa],[idKorisnik]) "
				+ "VALUES (?,?,?,?,?)";
		try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);){
			statement.setInt(1, addressFrom);
			statement.setInt(2, addressTo);
			statement.setInt(3, packageType);
			statement.setBigDecimal(4, weight);
			statement.setInt(5, idUser);
			
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
			e.printStackTrace();
			return -1;
		} 
	}

	@Override
	public boolean rejectAnOffer(int packageId) {
		String sql = "UPDATE Isporuka SET [status] = 4 WHERE [idIsporuka] = ? AND [status] = 0";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, packageId);			
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {
			
			//e.printStackTrace();
		}
		return false;
	}
	Integer[] getAddressId(int packageId) {
		Integer[] ret = new Integer[3];
		String sql = "SELECT [idPolaznaAdresa], [idDolaznaAdresa],[idMagacin] FROM  [Isporuka] WHERE [idIsporuka] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, packageId);	
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					ret[0] = rs.getInt(1);
					ret[1] = rs.getInt(2);
					ret[2] = rs.getInt(3);
					return ret;
				}
				
			}
			
		} catch (SQLException e) {
			
			//e.printStackTrace();
			return null;
		}
		
		
		return ret;
	}

	

}
