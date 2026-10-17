package hrms.dao;

import hrms.model.User;
import hrms.ui.EntityConfigs;
import hrms.util.DBConnection;
import hrms.util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** DAO for user-account CRUD and password verification. */
public final class UserDAO extends GenericDAO {
    public UserDAO() { super(EntityConfigs.users()); }

    public User authenticate(String username, String password) throws SQLException {
        String sql = "SELECT user_id, username, full_name, role, password_hash FROM users WHERE username = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username.trim());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || !PasswordUtil.verify(password, result.getString("password_hash"))) return null;
                return new User(result.getInt("user_id"), result.getString("username"),
                    result.getString("full_name"), result.getString("role"));
            }
        }
    }
}
