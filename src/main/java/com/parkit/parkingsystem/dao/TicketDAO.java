package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class TicketDAO {

    private static final Logger logger = LogManager.getLogger("TicketDAO");

    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    public static enum getQueries {
        currentTicket,
        lastRecentTicket
    }

    /**
     * Creates a new ticket in database
     * @param ticket The ticket to save in database
     * @return {@code true} if the operation succeeds
     */
    public boolean saveTicket(Ticket ticket) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(DBConstants.SAVE_TICKET);
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            //ps.setInt(1,ticket.getId());
            ps.setInt(1,ticket.getParkingSpot().getId());
            ps.setString(2, ticket.getVehicleRegNumber());
            ps.setDouble(3, ticket.getPrice());
            ps.setTimestamp(4, Timestamp.valueOf(ticket.getInTime()));
            ps.setTimestamp(5, (ticket.getOutTime() == null)?null: (Timestamp.valueOf(ticket.getOutTime())));
            return ps.execute();
        } catch (Exception ex) {
            logger.error("Error fetching next available slot", ex);
            return false;
        } finally {
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
        }
    }

    /**
     * Retrieves a database ticket from its {@code VehicleRegNumber}
     * @param vehicleRegNumber License plate of the vehicle to retrieve ticket(s) for
     * @param queryType <ul>
     *                    <li> {@code getQueries.currentTicket}: retrieves for the ongoing ticket for the vehicle.
     *                    <li> {@code getQueries.lastRecentTicket}: retrieves the most last terminated ticket for the vehicle that is less that 15 days old.
     *                  </ul>
     * @return The retrieved Ticket
     */
    public Ticket getTicket(String vehicleRegNumber, getQueries queryType) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        Ticket ticket = null;
        try {
            con = dataBaseConfig.getConnection();
            String sqlQuery = queryType == getQueries.currentTicket ? DBConstants.GET_TICKET : DBConstants.GET_LAST_RECENT_TICKET;
            ps = con.prepareStatement(sqlQuery);
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            ps.setString(1,vehicleRegNumber);
            rs = ps.executeQuery();
            if(rs.next()) {
                ticket = new Ticket();
                ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)), false);
                ticket.setParkingSpot(parkingSpot);
                ticket.setId(rs.getInt(2));
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(rs.getDouble(3));
                ticket.setInTime(rs.getTimestamp(4).toLocalDateTime());
                ticket.setOutTime((rs.getTimestamp(5) == null)? null: rs.getTimestamp(5).toLocalDateTime());
            }
            return ticket;
        } catch (Exception ex) {
            logger.error("Error retrieving ticket from database", ex);
            return null;
        } finally {
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
        }
    }

    /**
     * Updates in the database, the ticket with the same Id as the given {@code ticket}
     * @param ticket The new representation of the ticket
     * @return {@code true} if the operation succeeds
     */
    public boolean updateTicket(Ticket ticket) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(DBConstants.UPDATE_TICKET);
            ps.setDouble(1, ticket.getPrice());
            ps.setTimestamp(2, Timestamp.valueOf(ticket.getOutTime()));
            ps.setInt(3,ticket.getId());
            ps.execute();
            return true;
        } catch (Exception ex) {
            logger.error("Error saving ticket info", ex);
        } finally {
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
        }
        return false;
    }
}
