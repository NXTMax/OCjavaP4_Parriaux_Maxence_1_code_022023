package com.parkit.parkingsystem.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.Generated;

import java.util.Scanner;

@Generated
public class InputReaderUtil {

    private static Scanner scan = new Scanner(System.in, "utf-8");
    private static final Logger logger = LogManager.getLogger("InputReaderUtil");

    /**
     * Reads a line in the standard input and parses it into an integer
     * @return the read number
     */
    public int readSelection() {
        try {
            int input = Integer.parseInt(scan.nextLine());
            return input;
        } catch(Exception e) {
            logger.error("Error while reading user input from Shell", e);
            System.out.println("Error reading input. Please enter valid number for proceeding further");
            return -1;
        }
    }

    /**
     * Reads a line in the standard input
     * @return the read line
     * @throws IllegalArgumentException if the provided string is null or empty
     */
    public String readVehicleRegistrationNumber() throws IllegalArgumentException {
        try {
            String vehicleRegNumber= scan.nextLine();
            if(vehicleRegNumber == null || vehicleRegNumber.trim().length()==0) {
                throw new IllegalArgumentException("Invalid input provided");
            }
            return vehicleRegNumber;
        } catch(Exception e) {
            logger.error("Error while reading user input from Shell", e);
            System.out.println("Error reading input. Please enter a valid string for vehicle registration number");
            throw e;
        }
    }


}
