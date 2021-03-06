package com.tisawesomeness.minecord.database.dao;

import com.tisawesomeness.minecord.database.Database;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@RequiredArgsConstructor
public class CommandStats {

    private static final String UPDATE_SQL = "UPDATE command SET uses = uses + ? WHERE id = ?;";
    private static final String INSERT_SQL =
            "INSERT INTO command (id, uses)\n" +
            "SELECT ?, ?\n" +
            "WHERE NOT EXISTS (\n" +
            "    SELECT 1 FROM command WHERE id = ?\n" +
            ");";

    private final Database db;

    /**
     * Queries the database for the number of times each command has been used.
     * @return A Multiset of strings where each key is a command ID and its count is the number of uses
     * @throws SQLException When a database error occurs
     */
    public Multiset<String> getCommandUses() throws SQLException {
        @Cleanup Connection connect = db.getConnect();
        @Cleanup Statement st = connect.createStatement();
        ResultSet rs = st.executeQuery(
                "SELECT * FROM command;"
        );
        Multiset<String> commandUses = HashMultiset.create();
        while (rs.next()) {
            commandUses.add(rs.getString("id"), rs.getInt("uses"));
        }
        return ImmutableMultiset.copyOf(commandUses);
    }

    /**
     * Adds the provided uses to the running count in the database.
     * <br>This does not clear the input.
     * @param commandUses A Multiset of strings where each key is a command ID and its count is the number of uses
     * @throws SQLException When a database error occurs
     */
    public void pushCommandUses(Multiset<String> commandUses) throws SQLException {
        @Cleanup Connection connect = db.getConnect();
        connect.setAutoCommit(false);
        @Cleanup PreparedStatement updateSt = connect.prepareStatement(UPDATE_SQL);
        @Cleanup PreparedStatement insertSt = connect.prepareStatement(INSERT_SQL);
        try {
            for (Multiset.Entry<String> entry : commandUses.entrySet()) {
                String id = entry.getElement();
                int uses = entry.getCount();
                updateSt.setInt(1, uses);
                updateSt.setString(2, id);
                updateSt.executeUpdate();
                insertSt.setString(1, id);
                insertSt.setInt(2, uses);
                insertSt.setString(3, id);
                insertSt.executeUpdate();
            }
            connect.commit();
        } catch (Exception ex) {
            connect.rollback();
            throw ex;
        }
    }

}
