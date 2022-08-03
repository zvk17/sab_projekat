package rs.etf.sab.student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import rs.etf.sab.operations.UserOperations;

public class MyUserOperations implements UserOperations {
	
	Connection connection = DB.getInstance().getConnection();
	
	@Override
	public boolean declareAdmin(String userName) {
		int idUser = getIdUser(userName);
		if (idUser != -1) {
			String sql = "insert into Administrator ([idAdministrator]) VALUES(?)";
			try (PreparedStatement statement = connection.prepareStatement(sql);){
				statement.setInt(1, idUser);				
				int result = statement.executeUpdate();				
					
				return result != 0;
				
				
			} catch (SQLException e) {
				//System.out.println("Nije uspelo insertovanje");
				//e.printStackTrace();
				return false;
			}
			
		}
		
		return false;
	}

	@Override
	public int deleteUsers(String... usernames) {
		int count = 0;
		for (String username: usernames) {			
			
			try (PreparedStatement statement = connection.prepareStatement("DELETE from Korisnik WHERE korisnickoIme = ?");){
				statement.setString(1, username);
				count += statement.executeUpdate();
			} catch (SQLException e) {
				//e.printStackTrace();				
			}

		}
		return count;
	}

	@Override
	public List<String> getAllUsers() {
		List<String> usernamesList = new ArrayList<>();
		try (PreparedStatement statement = connection.prepareStatement("select [korisnickoIme] from [Korisnik]");){
			
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
	public int getSentPackages(String... userNames) {
		List<Integer> idUserList = new ArrayList<>();
		for(String username: userNames) {
			int idUser = getIdUser(username);
			if (idUser != -1) {
				idUserList.add(idUser);
			}			
		}
		if (idUserList.size() == 0)  {
			return -1;
		}
		int count = 0;
		String sql = "SELECT COUNT(*) AS BrojIsporuka FROM Isporuka WHERE idUser = ?";
		for (Integer idUser: idUserList) {
			try (PreparedStatement statement = connection.prepareStatement(sql);){
				statement.setInt(1, idUser);
				try (ResultSet rs = statement.executeQuery();) {
					if (rs.next()) {
						count += rs.getInt(1);
					}
				}				
				
			} catch (SQLException e) {
				//e.printStackTrace();
				
			}
		}
		return count;
	}

	@Override
	public boolean insertUser(String userName, String firstName, String lastName, String password, int idAddress) {
		if (!checkName(firstName)) {
			return false;
		}
		if (!checkName(lastName)) {
			return false;
		}
		if (!checkPassword(password)) {
			return false;
		}
		String sql = "INSERT INTO Korisnik ([ime] ,[prezime] ,[korisnickoIme] ,[sifra] ,[idAdresa]) VALUES(?,?,?,?,?)";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setString(1, firstName);
			statement.setString(2, lastName);
			statement.setString(3, userName);
			statement.setString(4, password);
			statement.setInt(5, idAddress);
			int result = statement.executeUpdate();
			
				
			return result != 0;
			
			
		} catch (SQLException e) {
			//System.out.println("Nije uspelo insertovanje");
			//e.printStackTrace();
			return false;
		} 
	}

	protected int getIdUser(String username) {
		int idUser = -1;
		String sql = "SELECT idKorisnik from [Korisnik] WHERE [korisnickoIme] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setString(1, username);
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					idUser = rs.getInt(1);		
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return idUser;
	}
	protected boolean checkName(String name) {
		if (name == null) {
			return false;
		}
		
		return Character.isUpperCase(name.charAt(0));
	}
	protected boolean checkPassword(String password) {
		if (password == null) {
			return false;
		}
		if (password.length() < 8) {
			return false;
		}
		boolean hasLower = false;
		boolean hasUpper = false;
		boolean hasDigit = false;
		boolean hasSpecialSymbol = false;
		
		for(int i = 0; i< password.length(); i++) {
			char c = password.charAt(i);
			if (Character.isLowerCase(c)) {
				hasLower = true;
			}
			if (Character.isUpperCase(c)) {
				hasUpper = true;
			}
			if (Character.isDigit(c)) {
				hasDigit = true;
			}
			if (Character.isWhitespace(c)) {
				return false;
			}
			hasSpecialSymbol = true;
			
		}
		return hasLower && hasUpper && hasDigit && hasSpecialSymbol;

	}
	int getAddress(int idUser) {
		System.out.println("getAddress");
		if (idUser == -1) return -1;
		int idAddress = -1;		
		String sql = "SELECT [idAdresa] FROM  [Korisnik] WHERE [idKorisnik] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, idUser);	
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					idAddress = rs.getInt(1);		
				}
				
			}
			
		} catch (SQLException e) {
			
			//e.printStackTrace();
		}
		return idAddress;

	}
	int getCity(int idUser) {
		System.out.println("getCity");
		if (idUser == -1) return -1;
		int idCity = -1;
		int idAddress = this.getAddress(idUser);
		if (idAddress == -1) return -1;
		System.out.println("adresa nadjena: " + idAddress);
		String sql = "SELECT [idGrad] FROM  [Adresa] WHERE [idAdresa] = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql);){
			statement.setInt(1, idAddress);	
			try (ResultSet rs = statement.executeQuery();){
				
				if (rs.next()) {
					idCity = rs.getInt(1);		
				}
				
			}
			
		} catch (SQLException e) {
			
			//e.printStackTrace();
		}
		return idCity;

	}
}
