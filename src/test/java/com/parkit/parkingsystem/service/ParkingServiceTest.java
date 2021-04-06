package com.parkit.parkingsystem.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

	private static ParkingService parkingService;

	@Mock
	private static InputReaderUtil inputReaderUtil;
	@Mock
	private static ParkingSpotDAO parkingSpotDAO;
	@Mock
	private static TicketDAO ticketDAO;

	@BeforeEach
	private void setUpPerTest() {
		parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
	}

	@Test
	@DisplayName("Entrée véhicule avec place disponible")
	public void getNextParkingNumberAvailableTest() {
		// GIVEN
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
		// WHEN
		ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
		// THEN
		assertNotNull(parkingSpot);
	}

	@Test
	@DisplayName("Erreur place indisponible")
	public void getNextParkingNumberUnavailableTest() {
		// GIVEN
		when(inputReaderUtil.readSelection()).thenReturn(2);
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);
		try {
			// WHEN
			parkingService.getNextParkingNumberIfAvailable();
		} catch (Exception e) {
			String errorExceptionMsg = "Error fetching parking number from DB. Parking slots might be full";
			// THEN
			assertEquals(errorExceptionMsg, e.getMessage());
		}
	}

	@Test
	@DisplayName("Erreur du choix du type de véhicule")
	public void incorrectInputProvidedForVehicleTypeTest() {
		try {
			// GIVEN
			when(inputReaderUtil.readSelection()).thenReturn(4);

			// WHEN
			parkingService.getNextParkingNumberIfAvailable();
		} catch (IllegalArgumentException e) {
			// THEN
			assertThrows(IllegalArgumentException.class, () -> parkingService.getNextParkingNumberIfAvailable());
		}
	}

	@Test
	@DisplayName("Sortie du véhicule")
	public void processExitingVehicleTest() throws Exception {
		// GIVEN
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		Ticket ticket = new Ticket();
		ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
		ticket.setParkingSpot(parkingSpot);
		ticket.setVehicleRegNumber("ABCDEF");
		when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
		when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
		when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
		// WHEN
		parkingService.processExitingVehicle();
		// THEN
		verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
	}

}