package user_interface.customer;

import config.DatabaseConnection;
import dao.impl.CustomerImplDAO;
import dao.impl.ServiceRequestImplDAO;
import dao.impl.TaxiImplDAO;
import manager.ReportManager;
import manager.ServiceManager;
import models.*;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import javax.swing.*;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import services.GeocodingService;
import services.Sendable;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import javax.swing.*;
import java.awt.*;


public class MainMap {

    // System engine components
    private static ServiceManager serviceManager;
    private static CustomerImplDAO customerDAO;
    private static ServiceRequestImplDAO serviceRequestsDAO;
    private static TaxiImplDAO taxiDAO;

    // The customer who will use the application in this session
    private static Customer loggedCustomer = null;

    public static void main(String[] args) {
        // Prevent User-Agent blocks in OpenStreetMap
        System.setProperty("http.agent", "MyTaxiProject_v1");

        // 1. Initialize Database Infrastructure and Services
        try {
            Connection connection = DatabaseConnection.getConnection(); // Your connection class

            // Instantiate DAOs
            customerDAO = new CustomerImplDAO(connection);
            taxiDAO = new TaxiImplDAO(connection);
            serviceRequestsDAO = new ServiceRequestImplDAO(connection);

            // Instantiate business managers
            ReportManager reportManager = new ReportManager(serviceRequestsDAO);
            Sendable messenger = message -> JOptionPane.showMessageDialog(null, message, "Taxi System", JOptionPane.INFORMATION_MESSAGE);

            serviceManager = new ServiceManager(reportManager, messenger, serviceRequestsDAO, taxiDAO);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Critical error connecting to the Database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // 2. Safely launch the graphical interface on the Swing thread
        SwingUtilities.invokeLater(() -> {
            // STEP A: Mandatory Login or Registration
            showAuthenticationModule();

            if (loggedCustomer == null) {
                // If the user closed the window without logging in, abort the program
                System.exit(0);
            }

            // STEP B: Request pickup address
            String destinationAddress = JOptionPane.showInputDialog(null,
                    "Welcome, " + loggedCustomer.getFirstName() + "!\nWhat address are you requesting the taxi for?",
                    "Request Service", JOptionPane.QUESTION_MESSAGE);

            if (destinationAddress == null || destinationAddress.trim().isEmpty()) {
                System.exit(0);
            }

            // STEP C: Geolocate address using OpenStreetMap API
            Position customerPos = GeocodingService.getCoordinates(destinationAddress);
            if (customerPos == null) {
                JOptionPane.showMessageDialog(null, "Could not find the entered address.", "Location Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }

            // STEP D: Create the request in memory and attempt automatic assignment
            ServiceRequest newService = new ServiceRequest();
            newService.setCustomer(loggedCustomer);

            // Create an adapted business Position object
            Position customerPositionObj = new Position();
            customerPositionObj.setLatitude(customerPos.getLatitude());
            customerPositionObj.setLongitude(customerPos.getLongitude());
            newService.setCustomerPosition(customerPositionObj);

            // Assign default parameters for testing
            newService.setTaxirequired(TaxiType.STANDARD);
            newService.setServiceStatus(ServiceStatus.PENDING);
            newService.setRequestTime(LocalDateTime.now());

            // Process the service dynamically (Looks for a taxi, updates DB and inserts into Services)
            serviceManager.registeredService(newService);

            ServiceRequest finalServiceMapped = newService;
            try {
                if (newService.getServiceCode() > 0) {
                    finalServiceMapped = serviceRequestsDAO.findById(newService.getServiceCode())
                            .orElse(newService);
                }
            } catch (SQLException e) {
                System.err.println("Error al recuperar el servicio fresco para el mapa: " + e.getMessage());
            }

            // STEP E: Launch the Main Map pointing to the current state
            buildVisualMap(newService);
        });
    }

    /**
     * Login and registration module in a Swing window
     */
    private static void showAuthenticationModule() {
        JTextField txtDni = new JTextField(10);
        JPanel panelLogin = new JPanel(new GridLayout(2, 1, 5, 5));
        panelLogin.add(new JLabel("Enter your DNI/NIE to access:"));
        panelLogin.add(txtDni);

        int result = JOptionPane.showConfirmDialog(null, panelLogin, "Passenger Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String enteredDni = txtDni.getText().trim();
            if (enteredDni.isEmpty()) {
                JOptionPane.showMessageDialog(null, "DNI cannot be empty.");
                showAuthenticationModule();
                return;
            }

            try {
                // Search if it exists in the database through the smart Optional container
                Optional<Customer> customerOpt = customerDAO.findByNationalId(enteredDni);

                if (customerOpt.isPresent()) {
                    loggedCustomer = customerOpt.get();
                } else {
                    // DNI does not exist: Automatic new customer registration flow
                    int response = JOptionPane.showConfirmDialog(null,
                            "The DNI is not registered in the system.\nDo you want to sign up as a new customer?",
                            "Customer Registration", JOptionPane.YES_NO_OPTION);

                    if (response == JOptionPane.YES_OPTION) {
                        showRegistrationForm(enteredDni);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Interactive form to register fields required by your CustomerDAO
     */
    private static void showRegistrationForm(String dni) {
        JTextField txtFirstName = new JTextField();
        JTextField txtLastName = new JTextField();
        JTextField txtAge = new JTextField();
        JTextField txtPhone = new JTextField();

        JPanel panelForm = new JPanel(new GridLayout(4, 2, 5, 5));
        panelForm.add(new JLabel("Firstname:"));   panelForm.add(txtFirstName);
        panelForm.add(new JLabel("Lastname:"));    panelForm.add(txtLastName);
        panelForm.add(new JLabel("Age:"));         panelForm.add(txtAge);
        panelForm.add(new JLabel("Phone:"));       panelForm.add(txtPhone);

        int result = JOptionPane.showConfirmDialog(null, panelForm, "Registration Form", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                Customer newCustomer = new Customer();
                newCustomer.setFirstName(txtFirstName.getText().trim());
                newCustomer.setLastName(txtLastName.getText().trim());
                newCustomer.setAge(Integer.parseInt(txtAge.getText().trim()));
                newCustomer.setNationalId(dni);
                newCustomer.setPhoneNumber(txtPhone.getText().trim());

                // Persist in SQLite via the DAO
                int generatedId = customerDAO.createCustomer(newCustomer);

                // Load into session
                loggedCustomer = newCustomer;
                JOptionPane.showMessageDialog(null, "Registration successfully completed! Welcome.");

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Incorrect data. Please make sure to enter a valid age.");
                showRegistrationForm(dni);
            }
        }
    }


    private static void buildVisualMap(ServiceRequest service) {
        JFrame frame = new JFrame("Taxi Management App - Service Tracking");
        JXMapViewer mapViewer = new JXMapViewer();


        TileFactoryInfo info = new OSMTileFactoryInfo("OpenStreetMap", "https://tile.openstreetmap.org");
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        tileFactory.setThreadPoolSize(8);
        mapViewer.setTileFactory(tileFactory);


        GeoPosition customerGeoPos = new GeoPosition(
                service.getCustomerPosition().getLatitude(),
                service.getCustomerPosition().getLongitude()
        );

        ArrayList<Painter<JXMapViewer>> painters = new ArrayList<>();


        painters.add((g, map, w, h) -> {
            Point2D point = map.convertGeoPositionToPoint(customerGeoPos);
            int x = (int) point.getX();
            int y = (int) point.getY();

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int radio = 18;
            g2.setColor(Color.BLACK);
            g2.fillOval(x - (radio / 2), y - (radio / 2), radio, radio);
            g2.setColor(Color.BLUE);
            g2.fillOval(x - ((radio - 4) / 2), y - ((radio - 4) / 2), radio - 4, radio - 4);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            g2.drawString("C", x - 3, y + 4);

            g2.dispose();
        });


        GeoPosition taxiGeoPos;

        if (service.getTaxi() != null && service.getTaxi().getPosition() != null) {
            taxiGeoPos = new GeoPosition(
                    service.getTaxi().getPosition().getLatitude(),
                    service.getTaxi().getPosition().getLongitude()
            );

            painters.add((g, map, w, h) -> {
                Point2D point = map.convertGeoPositionToPoint(taxiGeoPos);
                int x = (int) point.getX();
                int y = (int) point.getY();


                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int radio = 18;
                g2.setColor(Color.BLACK);
                g2.fillOval(x - (radio / 2), y - (radio / 2), radio, radio);
                g2.setColor(Color.YELLOW);
                g2.fillOval(x - ((radio - 4) / 2), y - ((radio - 4) / 2), radio - 4, radio - 4);
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Arial", Font.BOLD, 11));
                g2.drawString("T", x - 3, y + 4);

                g2.dispose();
            });
        } else {
            taxiGeoPos = null;
        }

        org.jxmapviewer.painter.CompoundPainter<JXMapViewer> compoundPainter =
                new org.jxmapviewer.painter.CompoundPainter<>(painters);
        mapViewer.setOverlayPainter(compoundPainter);


        if (taxiGeoPos != null) {
            double midLat = (customerGeoPos.getLatitude() + taxiGeoPos.getLatitude()) / 2;
            double midLon = (customerGeoPos.getLongitude() + taxiGeoPos.getLongitude()) / 2;
            mapViewer.setAddressLocation(new GeoPosition(midLat, midLon));
            mapViewer.setZoom(3);
        } else {
            mapViewer.setAddressLocation(customerGeoPos);
            mapViewer.setZoom(3);
        }


        JButton btnCancel = new JButton("Cancel Taxi Request");
        btnCancel.setBackground(new Color(220, 53, 69)); // Rojo llamativo
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFont(new Font("Arial", Font.BOLD, 14));


        btnCancel.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame,
                    "Are you sure you want to cancel this service?",
                    "Confirm Cancellation", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                Position currentTaxiPosition = null;
                if (service.getTaxi() != null) {
                    currentTaxiPosition = service.getTaxi().getPosition();
                } else {
                    currentTaxiPosition = service.getCustomerPosition();
                }

                serviceManager.endService(service, currentTaxiPosition);
                frame.dispose();
            }
        });


        frame.setLayout(new BorderLayout());
        frame.add(mapViewer, BorderLayout.CENTER);
        frame.add(btnCancel, BorderLayout.SOUTH);
        frame.setSize(1024, 768);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}