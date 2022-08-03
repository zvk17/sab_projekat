package rs.etf.sab.student;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import rs.etf.sab.operations.GeneralOperations;

public class MyGeneralOperations implements GeneralOperations {
	Connection connection = DB.getInstance().getConnection();
	@Override
	public void eraseAll() {
		String sql = "{call spIsprazniBazu}";
		try {
			CallableStatement statement = connection.prepareCall(sql);
			statement.execute();
		} catch (SQLException e) {
			
			e.printStackTrace();
		}


	}

}
