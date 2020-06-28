package com.tisawesomeness.minecord.database;

import com.tisawesomeness.minecord.Config;
import com.tisawesomeness.minecord.util.RequestUtils;

import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.Cleanup;
import lombok.NonNull;
import org.sqlite.SQLiteDataSource;
import org.sqlite.javax.SQLiteConnectionPoolDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Database {

	private final DataSource source;
	private final LoadingCache<Long, Optional<DbGuild>> guilds;
	private final LoadingCache<Long, Optional<DbUser>> users;

	private Connection getConnect() throws SQLException {
		return source.getConnection();
	}

	/**
	 * Sets up the database connection pool and creates the caches.
	 * @throws SQLException when either the initial read or creating a missing table fails.
	 */
	public Database(Config config) throws SQLException {
		
		// Build database source
		String url = "jdbc:sqlite:" + config.dbPath;
		SQLiteDataSource ds = new SQLiteConnectionPoolDataSource();
		ds.setUrl(url);
		source = ds;

		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder()
				.expireAfterAccess(10, TimeUnit.MINUTES);
		if (config.debugMode) {
			builder.recordStats();
		}
		guilds = builder.build(new CacheLoader<Long, Optional<DbGuild>>() {
			@Override
			public Optional<DbGuild> load(@NonNull Long key) throws SQLException {
				return loadGuild(key);
			}
		});
		users = builder.build(new CacheLoader<Long, Optional<DbUser>>() {
			@Override
			public Optional<DbUser> load(@NonNull Long key) throws SQLException {
				return loadUser(key);
			}
		});

		// Create tables if they do not exist
		runScript("init.sql");
		
		// Add owner to elevated
		if (!config.owner.equals("0")) {
			changeElevated(Long.parseLong(config.owner), true);
		}

		System.out.println("Database connected.");
		
	}

	private Optional<DbGuild> loadGuild(@NonNull Long key) throws SQLException {
		@Cleanup Connection connect = getConnect();
		@Cleanup PreparedStatement st = connect.prepareStatement(
				"SELECT * FROM `guild` WHERE `id` = ?"
		);
		st.setLong(1, key);
		ResultSet rs = st.executeQuery();
		return rs.next() ? Optional.of(DbGuild.from(rs)) : Optional.empty();
	}
	private Optional<DbUser> loadUser(@NonNull Long key) throws SQLException {
		@Cleanup Connection connect = getConnect();
		@Cleanup PreparedStatement st = connect.prepareStatement(
				"SELECT * FROM `user` WHERE `id` = ?"
		);
		st.setLong(1, key);
		ResultSet rs = st.executeQuery();
		return rs.next() ? Optional.of(DbUser.from(rs)) : Optional.empty();
	}

	public boolean isElevated(long id) {
		return getUser(id).map(u -> u.elevated).orElse(false);
	}
	public boolean isBanned(long id) {
		return getUser(id).map(u -> u.banned).orElse(
				getGuild(id).map(g -> g.banned).orElse(false)
		);
	}

	/**
	 * Runs a .sql script from resources.
	 * <br>This assumes that each statement in the script is separated by semicolons.
	 * @param resourceName The filename of the script in the resources folder.
	 * @throws SQLException If there is an error executing the script.
	 */
	private void runScript(String resourceName) throws SQLException {
		String initScript = RequestUtils.loadResource(resourceName);
		@Cleanup Connection connect = getConnect();
		@Cleanup Statement statement = connect.createStatement();
		for (String query : Splitter.on(";").omitEmptyStrings().split(initScript)) {
			statement.executeUpdate(query);
		}
	}

	/**
	 * Either gets a guild from the cache or queries the database for it.
	 * <br>The guild is cleared from the cache once it is altered.
	 * @param id The guild id.
	 * @return The guild if present, or an empty Optional if not present in the database or an exception occured.
	 */
	public Optional<DbGuild> getGuild(long id) {
		try {
			return guilds.get(id);
		} catch (ExecutionException ex) {
			ex.printStackTrace();
		}
		return Optional.empty();
	}
	/**
	 * Either gets a user from the cache or queries the database for it.
	 * <br>The user is cleared from the cache once it is altered.
	 * @param id The user id.
	 * @return The user if present, or an empty Optional if not present in the database or an exception occured.
	 */
	public Optional<DbUser> getUser(long id) {
		try {
			return users.get(id);
		} catch (ExecutionException ex) {
			ex.printStackTrace();
		}
		return Optional.empty();
	}
	
	public void changePrefix(long id, String prefix) throws SQLException {
		@Cleanup Connection connect = getConnect();
		@Cleanup PreparedStatement st = connect.prepareStatement(
				"REPLACE INTO guild (id, prefix, lang, banned, noCooldown, deleteCommands, noMenu) VALUES(?, ?, " +
						"COALESCE((SELECT lang FROM (SELECT * FROM guild) AS temp WHERE id=?), NULL), " +
						"COALESCE((SELECT banned FROM (SELECT * FROM guild) AS temp WHERE id=?), 0), " +
						"COALESCE((SELECT noCooldown FROM (SELECT * FROM guild) AS temp WHERE id=?), 0), " +
						"COALESCE((SELECT deleteCommands FROM (SELECT * FROM guild) AS temp WHERE id=?), NULL), " +
						"COALESCE((SELECT noMenu FROM (SELECT * FROM guild) AS temp WHERE id=?), NULL));"
		);
		st.setLong(1, id);
		st.setString(2, prefix);
		st.setLong(3, id);
		st.setLong(4, id);
		st.setLong(5, id);
		st.setLong(6, id);
		st.setLong(7, id);
		st.executeUpdate();
		guilds.invalidate(id);
	}
	public void changeBannedGuild(long id, boolean banned) throws SQLException {
		@Cleanup Connection connect = getConnect();
		@Cleanup PreparedStatement st = connect.prepareStatement(
			"REPLACE INTO guild (id, prefix, lang, banned, noCooldown, deleteCommands, noMenu) VALUES(?, " +
			"COALESCE((SELECT prefix FROM (SELECT * FROM guild) AS temp WHERE id=?), NULL), " +
			"COALESCE((SELECT lang FROM (SELECT * FROM guild) AS temp WHERE id=?), NULL), ?, " +
			"COALESCE((SELECT noCooldown FROM (SELECT * FROM guild) AS temp WHERE id=?), 0), " +
			"COALESCE((SELECT deleteCommands FROM (SELECT * FROM guild) AS temp WHERE id=?), 0), " +
			"COALESCE((SELECT noMenu FROM (SELECT * FROM guild) AS temp WHERE id=?), 0));"
		);
		st.setLong(1, id);
		st.setLong(2, id);
		st.setLong(3, id);
		st.setBoolean(4, banned);
		st.setLong(5, id);
		st.setLong(6, id);
		st.setLong(7, id);
		st.executeUpdate();
		guilds.invalidate(id);
	}
	public void changeDeleteCommands(long id, boolean deleteCommands) throws SQLException {
		@Cleanup Connection connect = getConnect();
		@Cleanup PreparedStatement st = connect.prepareStatement(
			"REPLACE INTO guild (id, prefix, lang, banned, noCooldown, deleteCommands, noMenu) VALUES(?, " +
			"COALESCE((SELECT prefix FROM (SELECT * FROM guild) AS temp WHERE id=?), NULL), " +
			"COALESCE((SELECT lang FROM (SELECT * FROM guild) AS temp WHERE id=?), NULL), " +
			"COALESCE((SELECT banned FROM (SELECT * FROM guild) AS temp WHERE id=?), 0), " +
			"COALESCE((SELECT noCooldown FROM (SELECT * FROM guild) AS temp WHERE id=?), 0), ?, " +
			"COALESCE((SELECT noMenu FROM (SELECT * FROM guild) AS temp WHERE id=?), NULL));"
		);
		st.setLong(1, id);
		st.setLong(2, id);
		st.setLong(3, id);
		st.setLong(4, id);
		st.setLong(5, id);
		st.setBoolean(6, deleteCommands);
		st.setLong(7, id);
		st.executeUpdate();
		guilds.invalidate(id);
	}
	public void changeUseMenu(long id, boolean useMenu) throws SQLException {
		@Cleanup Connection connect = getConnect();
		@Cleanup PreparedStatement st = connect.prepareStatement(
			"REPLACE INTO guild (id, prefix, lang, banned, noCooldown, deleteCommands, noMenu) VALUES(?, " +
			"COALESCE((SELECT prefix FROM (SELECT * FROM guild) AS temp WHERE id=?), NULL), " +
			"COALESCE((SELECT lang FROM (SELECT * FROM guild) AS temp WHERE id=?), NULL), " +
			"COALESCE((SELECT banned FROM (SELECT * FROM guild) AS temp WHERE id=?), 0), " +
			"COALESCE((SELECT noCooldown FROM (SELECT * FROM guild) AS temp WHERE id=?), 0), " +
			"COALESCE((SELECT deleteCommands FROM (SELECT * FROM guild) AS temp WHERE id=?), NULL), ?);"
		);
		st.setLong(1, id);
		st.setLong(2, id);
		st.setLong(3, id);
		st.setLong(4, id);
		st.setLong(5, id);
		st.setLong(6, id);
		st.setBoolean(7, !useMenu);
		st.executeUpdate();
		guilds.invalidate(id);
	}
	
	public void changeElevated(long id, boolean elevated) throws SQLException {
		@Cleanup Connection connect = getConnect();
		@Cleanup PreparedStatement st = connect.prepareStatement(
			"REPLACE INTO user (id, elevated, banned) VALUES(?, ?, " +
			"COALESCE((SELECT banned FROM (SELECT * FROM user) AS temp WHERE id=?), 0));"
		);
		st.setLong(1, id);
		st.setBoolean(2, elevated);
		st.setLong(3, id);
		st.executeUpdate();
		users.invalidate(id);
	}
	public void changeBannedUser(long id, boolean banned) throws SQLException {
		@Cleanup Connection connect = getConnect();
		@Cleanup PreparedStatement st = connect.prepareStatement(
			"REPLACE INTO user (id, elevated, banned) VALUES(?, " +
			"COALESCE((SELECT banned FROM (SELECT * FROM user) AS temp WHERE id=?), 0), ?);"
		);
		st.setLong(1, id);
		st.setLong(2, id);
		st.setBoolean(3, banned);
		st.executeUpdate();
		users.invalidate(id);
	}

}
