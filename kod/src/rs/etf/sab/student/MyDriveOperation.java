package rs.etf.sab.student;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import rs.etf.sab.operations.DriveOperation;

public class MyDriveOperation implements DriveOperation {
	Connection connection = DB.getInstance().getConnection();
	MyUserOperations myUserOperations = new MyUserOperations();
	MyCourierOperations myCourierOperations = new MyCourierOperations();
	MyVehicleOperations myVehicleOperations = new MyVehicleOperations();
	MyAddressOperations myAddressOperations = new MyAddressOperations();
	MyPackageOperations myPackageOperations = new MyPackageOperations();
	private static final int MOVE_PHASE_1 = -100;
	private static final int MOVE_PHASE_2 = -200;
	private static final int MOVE_PHASE_3 = -300;
	@Override
	public List<Integer> getPackagesInVehicle(String courierUsername) {
		int idUser = myUserOperations.getIdUser(courierUsername);
		List<Integer> packagesList = new ArrayList<>();
		if (idUser == -1 ) {
			return packagesList;
		}
		int idDriveActive = this.getIdDriveActive(idUser);
		if (idDriveActive == -1) {
			return packagesList;
		}
		//int phase = getPhase(idDriveActive);
		List<Integer> preuzeti = getPreuzetiPackages(idDriveActive);
		
		packagesList.addAll(preuzeti);
		
		List<Integer> zaSlanje = getZaSlanjePackages(idDriveActive);
		
		packagesList.addAll(zaSlanje);
		
		
		return packagesList;
		
		
	}	

	


	@Override
	public int nextStop(String courierUsername) {
		System.out.println("nextStop " + courierUsername);
		int idUser = myUserOperations.getIdUser(courierUsername);
		List<Integer> packagesList = new ArrayList<>();
		if (idUser == -1 ) {
			return -5;
		}
		int idDriveActive = this.getIdDriveActive(idUser);
		if (idDriveActive == -1) {
			return -7;
		}
		int phase = getPhase(idDriveActive);
		int status;
		switch (phase) {
			case 0: {
				status = doPhase0(idUser, idDriveActive);
				if (status != MOVE_PHASE_1) {
					return status;
				}
				
			}
				
				//break;
			case 1: {
				status = doPhase1(idUser, idDriveActive);
				if (status != MOVE_PHASE_2) {
					return status;
				}
			}
			case 2:
				status = doPhase2(idUser, idDriveActive);
				if (status != MOVE_PHASE_3) {
					return status;
				}

			case 3:
				return doPhase3(idUser, idDriveActive);
				//break;
			
			default:
				break;
		
		}
		
		return 0;
	}
	private int doPhase0(int idUser, int idDrive) {
		System.out.println("doPhase0");
		String vehiclePlateNumber = getDriveVehicle(idDrive);
		int idDriveAddress = getDriveAddress(idDrive);
		int idCity = myAddressOperations.getIdCityFromAddress(idDriveAddress); 
		BigDecimal capacity = myVehicleOperations.getCapacity(vehiclePlateNumber);
		BigDecimal opterecenje = getDriveOpterecenje(idDrive);
		BigDecimal slobodanProstor = capacity.subtract(opterecenje);
		if (capacity == null || opterecenje == null) {
			System.err.println("Null MyDriveOperation.doPhase0");
			return -10;
		}
		Timestamp date = getDriveDate(idDrive);

		int idPackage = findNextPackageAddress(idCity, slobodanProstor, date);
		if (idPackage == -1) {
			
			updatePhase(idDrive, 1);
			return MOVE_PHASE_1;
		}
		System.out.println("Skupljam paket-direktno " + idPackage);
		BigDecimal tezinaPaketa = this.myPackageOperations.getWeightOfPackage(idPackage);
		myPackageOperations.updateDeliveryStatus(idPackage, 2);

		insertSlanjeIsporuke(idPackage, idDrive);

		Integer[] addresses = myPackageOperations.getAddressId(idPackage);
		updatePredjeniPut(idDrive, idDriveAddress, addresses[0]);//menja mesto u bazi
		updateTrosakPredjenogPuta(idDrive, vehiclePlateNumber, idDriveAddress, addresses[0]);
		increaseOpterecenje(idDrive, tezinaPaketa);
		return -2;
	}
	private int doPhase1(int idUser, int idDrive) {
		System.out.println("doPhase1  utovar iz magacina");
		int idDriveAddress = getDriveAddress(idDrive);
		int idCity = myAddressOperations.getIdCityFromAddress(idDriveAddress); 
		String vehiclePlateNumber = getDriveVehicle(idDrive);
		BigDecimal capacity = myVehicleOperations.getCapacity(vehiclePlateNumber);
		BigDecimal opterecenje = getDriveOpterecenje(idDrive);
		BigDecimal slobodanProstor = capacity.subtract(opterecenje);
		
		int idPackage = findNextPackageInStockroomAddress(idCity, slobodanProstor);
		if (idPackage == -1) {
			System.out.println("U magacinu nema paketa");
			updatePhase(idDrive, 2);
			return MOVE_PHASE_2;
		}
		Integer[] addresses = myPackageOperations.getAddressId(idPackage);
		updatePredjeniPut(idDrive, idDriveAddress, addresses[2]);//menja mesto u bazi
		updateTrosakPredjenogPuta(idDrive, vehiclePlateNumber, idDriveAddress, addresses[2]);
		
		while (idPackage != -1) {
			BigDecimal tezinaPaketa = this.myPackageOperations.getWeightOfPackage(idPackage);			

			insertSlanjeIsporuke(idPackage, idDrive);			
			
			increaseOpterecenje(idDrive, tezinaPaketa);
			
			removePackageFromStockroom(idPackage);
			
			slobodanProstor = slobodanProstor.subtract(tezinaPaketa);
			idPackage = findNextPackageInStockroomAddress(idCity, slobodanProstor);
		}
		
		updatePhase(idDrive, 2);
		return -2;

	}
	private int doPhase2(int idUser, int idDrive) {
		System.out.println("doPhase2");
		int idDriveAddress = getDriveAddress(idDrive);
		int idCity = myAddressOperations.getIdCityFromAddress(idDriveAddress); 
		String vehiclePlateNumber = getDriveVehicle(idDrive);
		
		
		int idPackage = findNextPackageForSending(idDrive, idCity, idDriveAddress);
		if (idPackage != -1) {
			System.out.println("Isporucujem paket " + idPackage);
			Integer[] addresses = myPackageOperations.getAddressId(idPackage);
			BigDecimal distance = myAddressOperations.getDistance(addresses[1], idDriveAddress);
			updatePredjeniPut(idDrive, idDriveAddress, addresses[1]);//menja mesto u bazi
			updateTrosakPredjenogPuta(idDrive, vehiclePlateNumber, idDriveAddress, addresses[1]);
			BigDecimal packageCost = myPackageOperations.getPriceOfDelivery(idPackage);
			BigDecimal packageWeight = myPackageOperations.getWeightOfPackage(idPackage);
			increaseOpterecenje(idDrive, packageWeight.negate());
			updatePrihod(idDrive, packageCost);
			myPackageOperations.updateDeliveryStatus(idPackage, 3);
			deleteFromSlanjeIsporuke(idPackage);
			this.myCourierOperations.incrementPackagesNumber(idUser);
			return idPackage;
		}
		
		BigDecimal capacity = myVehicleOperations.getCapacity(vehiclePlateNumber);
		BigDecimal opterecenje = getDriveOpterecenje(idDrive);
		BigDecimal slobodanProstor = capacity.subtract(opterecenje);
		idPackage = findNextForCollecting(idDrive, idCity, idDriveAddress, slobodanProstor);
		if (idPackage == -1 ) {
			idPackage = findNextPackageInStockroomAddress(idCity, slobodanProstor);
			if (idPackage != -1) {
				removePackageFromStockroom(idPackage);
				System.out.println("u magacinu nasao " + idPackage);
			} else {
				System.out.println("Nema vise paketa idem nazad u magacin");
				updatePhase(idDrive, 3);
				return MyDriveOperation.MOVE_PHASE_3;
			}
			
		}
		System.out.println("Skupljam pakete-magacin: " + idPackage);
		BigDecimal tezinaPaketa = this.myPackageOperations.getWeightOfPackage(idPackage);
		myPackageOperations.updateDeliveryStatus(idPackage, 2);

		insertPreuzetaIsporuka(idPackage, idDrive);

		Integer[] addresses = myPackageOperations.getAddressId(idPackage);
		updatePredjeniPut(idDrive, idDriveAddress, addresses[0]);//menja mesto u bazi
		updateTrosakPredjenogPuta(idDrive, vehiclePlateNumber, idDriveAddress, addresses[0]);
		increaseOpterecenje(idDrive, tezinaPaketa);
		
		return -2;
		
		
		
	}
	private int doPhase3(int idUser, int idDrive) {
		System.out.println("doPhase3");
		int idDriveAddress = getDriveAddress(idDrive);//trenutna adresa voznje
		int idCity = myAddressOperations.getIdCityFromAddress(idDriveAddress); 
		
		int courierAddress = myUserOperations.getAddress(idUser);
		int idCityStockroom = myAddressOperations.getIdCityFromAddress(courierAddress);//city magacina
		int idStockroomAddress = new MyStockroomOperations().getIdStockroom(idCityStockroom);
		String vehiclePlateNumber = getDriveVehicle(idDrive);
		
		updatePredjeniPut(idDrive, idDriveAddress, idStockroomAddress);
		updateTrosakPredjenogPuta(idDrive, vehiclePlateNumber, idDriveAddress, idStockroomAddress);
		List<Integer> preuzeti = getPreuzetiPackages(idDrive);
		for (int idPackage: preuzeti) {
			this.deleteFromPreuzetaIsporuka(idPackage);
			setPackageToStockroom(idPackage, idStockroomAddress);
			
		}
		this.myCourierOperations.updateCourierStatus(idUser, 0);
		BigDecimal profit = this.getProfit(idDrive);
		this.myCourierOperations.updateProfit(idUser, profit);
		
		resetOpterecenje(idDrive);
		this.myVehicleOperations.removeVehicleFromUse(vehiclePlateNumber);
		this.myVehicleOperations.parkVehicle(vehiclePlateNumber, idStockroomAddress);
		//System.out.println("parkrirao " + b);
		updatePhase(idDrive, 4);
		return -1;
	}



	private int findNextForCollecting(int idDrive, int idCity, int idDriveAddress, BigDecimal slobodanProstor) {
		String sql = "SELECT idIsporuka "
				+    "FROM [projekat_sab].[dbo].[Isporuka] i "
				+    "INNER JOIN Adresa a ON (i.idPolaznaAdresa = a.idAdresa) "
				+    "WHERE a.idGrad = ? "
				+    "AND tezinaPaketa < ? "
				//+    "AND i.[vremePrihvatanja] < ? "
				+    "AND i.status = 1 "
				+    "ORDER BY i.vremeKreiranja";
		int idPackage = -1;
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, idCity);
			statement.setBigDecimal(2, slobodanProstor);
			//statement.setTimestamp(3, date);
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					idPackage = rs.getInt(1);		
				}
				
			}
		} catch (SQLException e) {}
		if (idPackage != -1) {
			
		}
		
		
		return idPackage;
		
		/*String sqlCode = "SELECT idIsporuka "
				+    "FROM [projekat_sab].[dbo].[Isporuka] i "
				+    "INNER JOIN Adresa a ON (i.idMagacin = a.idAdresa) "
				+    "WHERE a.idGrad = ? "
				+    "AND tezinaPaketa < ? "
				//+    "AND i.[vremePrihvatanja] < ? "
				+    "AND i.status = 2 "
				+    "ORDER BY i.vremeKreiranja";
		
		try (PreparedStatement statement = connection.prepareStatement(sqlCode);){
			statement.setInt(1, idCity);
			statement.setBigDecimal(2, slobodanProstor);
			//statement.setTimestamp(3, date);
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					idPackage = rs.getInt(1);		
				}
				
			}
		} catch (SQLException e) {}
		return idPackage;*/
	}




	private int findNextPackageForSending(int idDrive, int idCity, int idAddress) {
		List<Integer> packagesList = getZaSlanjePackages(idDrive);
		int idPackageMin = -1;
		BigDecimal minDistance = null;
		for (int idPackage: packagesList) {
			Integer[] addresses = myPackageOperations.getAddressId(idPackage);
			if (minDistance == null) {
				idPackageMin = idPackage;
				
				
				minDistance = myAddressOperations.getDistance(addresses[1], idAddress);
			} else {
				BigDecimal newDistance = myAddressOperations.getDistance(addresses[1], idAddress);
				if (newDistance.compareTo(minDistance) == -1) {
					minDistance = newDistance;
					idPackageMin = idPackage;
				}
			}
		}
		return idPackageMin;
	}




	




	private int findNextPackageInStockroomAddress(int idCity, BigDecimal slobodanProstor) {
		String sql = "SELECT idIsporuka "
				+    "FROM [projekat_sab].[dbo].[Isporuka] i "
				+    "INNER JOIN Adresa a ON (i.[idMagacin] = a.idAdresa) "
				+    "WHERE a.idGrad = ? "
				+    "AND tezinaPaketa < ? "
				+    "AND i.[idMagacin] IS NOT NULL "
				+    "AND i.status = 2 "
				+    "ORDER BY i.vremeKreiranja";
		int idPackage = -1;
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, idCity);
			statement.setBigDecimal(2, slobodanProstor);
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					idPackage = rs.getInt(1);		
				}
				
			}
		} catch (SQLException e) {}
		
		return idPackage;

	}


	private int findNextPackageAddress(int idCity, BigDecimal slobodanProstor, Timestamp date) {

		String sql = "SELECT idIsporuka "
				+    "FROM [projekat_sab].[dbo].[Isporuka] i "
				+    "INNER JOIN Adresa a ON (i.idPolaznaAdresa = a.idAdresa) "
				+    "WHERE a.idGrad = ? "
				+    "AND tezinaPaketa < ? "
				+    "AND i.[vremePrihvatanja] < ? "
				+    "AND i.status = 1 "
				+    "ORDER BY i.vremeKreiranja";
		int idPackage = -1;
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, idCity);
			statement.setBigDecimal(2, slobodanProstor);
			statement.setTimestamp(3, date);
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					idPackage = rs.getInt(1);		
				}
				
			}
		} catch (SQLException e) {}
		return idPackage;
		
	}


	private boolean updatePredjeniPut(int idDrive, int idAddress1, int idAddress2) {
		BigDecimal distance = myAddressOperations.getDistance(idAddress1, idAddress2);
		//System.out.println("Predjeni put: " + distance);
		String sql1 = "UPDATE [Voznja]  SET [predjeniPut] = [predjeniPut] + ? WHERE idVoznja = ?"; 
		try (PreparedStatement statement = connection.prepareStatement(sql1);){
			statement.setBigDecimal(1, distance);
			statement.setInt(2, idDrive);
			boolean b = statement.executeUpdate() != 0;
			if (!b) return false;
		} catch (SQLException e) {}
		
		String sql2 = "UPDATE [Voznja]  SET [idAdresa] = ? WHERE idVoznja = ?"; 
		try (PreparedStatement statement = connection.prepareStatement(sql2);){
			statement.setInt(1, idAddress2);
			statement.setInt(2, idDrive);
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {}
		return false;
	}
	private boolean updateTrosakPredjenogPuta(int idDrive, String vehiclePlateNumber, int idAddress1, int idAddress2) {
		BigDecimal distance = this.myAddressOperations.getDistance(idAddress1, idAddress2);
		BigDecimal trosak = this.myVehicleOperations.getTrosak(vehiclePlateNumber, distance);
		//System.out.println("Trosak predjenog puta " + trosak);
		
		return updatePrihod(idDrive, trosak.negate());
		
	}
	private boolean updatePrihod(int idDrive, BigDecimal prihod) {
		
		String sql2 = "UPDATE [Voznja]  SET [prihod] = [prihod] + ? WHERE idVoznja = ?"; 
		try (PreparedStatement statement = connection.prepareStatement(sql2);){
			statement.setBigDecimal(1, prihod);
			statement.setInt(2, idDrive);
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {}
		return false;
	}
	private boolean increaseOpterecenje(int idDrive, BigDecimal weight) {
		
		String sql2 = "UPDATE [Voznja]  SET [opterecenostVozila] = [opterecenostVozila] + ? WHERE idVoznja = ?"; 
		try (PreparedStatement statement = connection.prepareStatement(sql2);){
			statement.setBigDecimal(1, weight);
			statement.setInt(2, idDrive);
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {}
		return false;
		
	}
	private boolean resetOpterecenje(int idDrive) {
		String sql2 = "UPDATE [Voznja]  SET [opterecenostVozila] = 0 WHERE idVoznja = ?"; 
		try (PreparedStatement statement = connection.prepareStatement(sql2);){
			statement.setInt(1, idDrive);
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {}
		return false;
		
	}

	private String getDriveVehicle(int idDrive) {
		String vehiclePlateNumber = null;
		String sql = "SELECT registracioniBroj from [Voznja] WHERE [idVoznja] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, idDrive);
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					vehiclePlateNumber = rs.getString(1);		
				}
				
			}
		} catch (SQLException e) {}
		
		return vehiclePlateNumber;
	}

	private int getDriveAddress(int idDrive) {
		int idAddress = -1;
		String sql = "SELECT idAdresa from [Voznja] WHERE [idVoznja] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, idDrive);
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					idAddress = rs.getInt(1);		
				}
				
			}
		} catch (SQLException e) {}
		
		return idAddress;
	}
	private BigDecimal getProfit(int idDrive) {
		BigDecimal profit = BigDecimal.valueOf(0);
		String sql = "SELECT [prihod] from [Voznja] WHERE [idVoznja] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, idDrive);
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					profit = rs.getBigDecimal(1);		
				}
				
			}
		} catch (SQLException e) {}
		
		return profit;
	}
	private BigDecimal getDriveOpterecenje(int idDrive) {
		BigDecimal opterecenje = null;

		String sql = "SELECT [opterecenostVozila] from [Voznja] WHERE [idVoznja] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, idDrive);
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					opterecenje = rs.getBigDecimal(1);		
				}
				
			}
		} catch (SQLException e) {}
		
		return opterecenje;
	}
	private Timestamp getDriveDate(int idDrive) {
		Timestamp time = null;

		String sql = "SELECT [vremeKreiranja] from [Voznja] WHERE [idVoznja] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, idDrive);
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					time = rs.getTimestamp(1);		
				}
				
			}
		} catch (SQLException e) {}
		
		return time;
	}
	private boolean updateDriveDate(int idDrive) {
		Timestamp time = null;

		String sql = "UPDATE [Voznja] SET [vremeKreiranja] = GETDATE() WHERE [idVoznja] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, idDrive);
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		
		return false;
	}




	@Override
	public boolean planingDrive(String courierUsername) {
		System.out.println("planingDrive");

		int idUser = this.myUserOperations.getIdUser(courierUsername);       //dohvati idKurira
		if (idUser == -1) return false;
		
		int idCity = this.myUserOperations.getCity(idUser);                  //dohvatiti grad kurira
		//System.out.println("grad:" + idCity);
		String vehiclePlateNumber = findFreeVehiclePlateNumberCity(idCity);  //pronaci slobodno vozilo u gradu odakle je
		if (vehiclePlateNumber == null)  return false;                       //ako nijedno vozilo nije pronadjeno
		int idStockroom = getParkedStockroom(vehiclePlateNumber);
		System.out.println("idstockroom addresa " + idStockroom);
		System.out.println("nadjeno vozilo: " + vehiclePlateNumber + " u gradu "+ idCity); 
		if (!unparkVehicle(vehiclePlateNumber)) return false;                //set idMagacin na null		
		if (!insertTrenutnoVozi(idUser, vehiclePlateNumber)) return false;   //insert into trenutnovozi
		
		if (!myCourierOperations.updateCourierStatus(idUser, 1)) return false;//set status vozaca na zauzet
		
		boolean b = insertNewDrive(vehiclePlateNumber, idUser, idStockroom, 0);
		System.out.println("planingDrive inserted " + b);
		return b;
	}
	
	
	
	




	private boolean insertNewDrive(String vehiclePlateNumber, int idUser, int idAddress, int phase) {
		System.out.println("insertNewDrive");
		String sql = "INSERT INTO [dbo].[Voznja] ([registracioniBroj] ,[idKurir] ,[idAdresa] ,[faza])"
				+    "VALUES (? ,? ,? ,?)";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			
			statement.setString(1, vehiclePlateNumber);
			statement.setInt(2, idUser);
			statement.setInt(3, idAddress);
			statement.setInt(4, phase);
			int result = statement.executeUpdate();			
			return result != 0;
			
			
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}




	private int getIdDriveActive(int idCourier) {
		int idDrive = -1;
		String sql = "SELECT idVoznja from [Voznja] WHERE [idKurir] = ? AND faza != 4";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, idCourier);
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					idDrive = rs.getInt(1);		
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return idDrive;
		
	}
	
	private int getPhase(int idDrive) {
		int idPhase = -1;
		String sql = "SELECT faza from [Voznja] WHERE [idVoznja] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, idDrive);
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					idPhase = rs.getInt(1);		
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return idPhase;
	}
	private boolean updatePhase(int driveId, int phase) {
		System.out.println("updatePhase driveId: " + driveId + " set to " + phase);
		String sql = "UPDATE [Voznja]  SET faza = ? WHERE idVoznja = ?"; 
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, phase);
			statement.setInt(2, driveId);
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {}
		return false;
	}
	
	private List<Integer> getPreuzetiPackages(int idDriveActive) {
		List<Integer> packageList = new ArrayList<>();
		String sql = "SELECT idIsporuka FROM PreuzetaIsporuka WHERE [idVoznja] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, idDriveActive);		
			try (ResultSet rs = statement.executeQuery();){
				
				while (rs.next()) {
					packageList.add(rs.getInt(1));				
				}
				
			}
		} catch (SQLException e) {
			//e.printStackTrace();
			packageList = new ArrayList<>();
			return packageList;
		}
		return packageList;
	}
	private List<Integer> getZaSlanjePackages(int idDriveActive) {
		List<Integer> packageList = new ArrayList<>();
		String sql = "SELECT idIsporuka FROM SlanjeIsporuke WHERE [idVoznja] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, idDriveActive);		
			try (ResultSet rs = statement.executeQuery();){
				
				while (rs.next()) {
					packageList.add(rs.getInt(1));				
				}
				
			}
		} catch (SQLException e) {
			//e.printStackTrace();
			packageList = new ArrayList<>();
			return packageList;
		}
		return packageList;
	}

	private boolean deleteFromPreuzetaIsporuka(int packageId) {
		String sql = "DELETE from PreuzetaIsporuka WHERE idIsporuka = ?"; 
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, packageId);
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {}
		return false;
	}
	
	private boolean deleteFromSlanjeIsporuke(int packageId) {
		String sql = "DELETE from SlanjeIsporuke WHERE idIsporuka = ?"; 
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, packageId);
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {}
		return false;
	}
	private boolean insertPreuzetaIsporuka(int packageId, int driveId) {
		String sql = "INSERT INTO [dbo].[PreuzetaIsporuka] "
				+    "([idVoznja] ,[idIsporuka]) VALUES (? ,?)";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			
			statement.setInt(1, driveId);
			statement.setInt(2, packageId);			
			int result = statement.executeUpdate();			
			return result != 0;
			
			
		} catch (SQLException e) {			
			return false;
		}
	}
	
	private boolean insertSlanjeIsporuke(int packageId, int driveId) {
		String sql = "INSERT INTO [dbo].[SlanjeIsporuke] "
				+    "([idVoznja] ,[idIsporuka]) VALUES (? ,?)";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			
			statement.setInt(1, driveId);
			statement.setInt(2, packageId);			
			int result = statement.executeUpdate();			
			return result != 0;
			
			
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	String findFreeVehiclePlateNumberCity(int idCity) {
		String vehiclePlateNumber = null;
		String sql = "SELECT registracioniBroj "
				+    "FROM [Vozilo] v INNER JOIN Magacin m on (m.idMagacin = v.idMagacin)"
				+    "WHERE idGrad = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, idCity);
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					vehiclePlateNumber = rs.getString(1);		
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return vehiclePlateNumber;
		
	}
	private int getParkedStockroom(String vehiclePlateNumber) {
		String sql = "SELECT  COALESCE([idMagacin],-1) AS idMag "
				+    "FROM Vozilo "
				+    "WHERE [registracioniBroj] = ?";
		int idStockroom = -1;
		
		
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setString(1, vehiclePlateNumber);
			try (ResultSet rs = statement.executeQuery();) {
				if (rs.next()) {
					idStockroom = rs.getInt(1);
				}
			}				
			
		} catch (SQLException e) {
			e.printStackTrace();
			
		}
	
		return idStockroom;
	}
	private boolean setPackageToStockroom(int idPackage, int idStockroom) {
		String sql = "UPDATE Isporuka SET [idMagacin] = ? WHERE [idIsporuka] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){	
			statement.setInt(1, idStockroom);	
			statement.setInt(2, idPackage);			
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {			
			//e.printStackTrace();
		}
		return false;
	}
	private boolean removePackageFromStockroom(int idPackage) {
		String sql = "UPDATE Isporuka SET [idMagacin] = NULL WHERE [idIsporuka] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){			
			statement.setInt(1, idPackage);			
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {			
			//e.printStackTrace();
		}
		return false;
	}
	private boolean unparkVehicle(String vehiclePlateNumber) {
		String sql = "UPDATE Vozilo SET [idMagacin] = NULL WHERE [registracioniBroj] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){			
			statement.setString(1, vehiclePlateNumber);			
			return statement.executeUpdate() != 0;
		} catch (SQLException e) {			
			//e.printStackTrace();
		}
		return false;
	}
	private boolean insertTrenutnoVozi(int idUser, String vehiclePlateNumber) {
		String sql = "INSERT INTO [dbo].[VoziTrenutno] ([registracioniBroj] ,[idKurir]) VALUES (?,?)";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			
			statement.setString(1, vehiclePlateNumber);
			statement.setInt(2, idUser);			
			int result = statement.executeUpdate();			
			return result != 0;
			
			
		} catch (SQLException e) {
			return false;
		} 
	}
	
	

}
