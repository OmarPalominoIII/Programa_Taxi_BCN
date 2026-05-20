package user_interface.admin;

import config.DatabaseConnection;
import dao.impl.DriverImplDAO;
import dao.impl.TaxiImplDAO;
import manager.ServiceManager;
import manager.ReportManager;
import models.*;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.viewer.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;

import javax.swing.*;
import java.awt.*;


public class UIMenuAdmin extends JFrame {

    private final ServiceManager serviceManager;
    private final ReportManager reportManager;
    private final TaxiImplDAO taxiDAO;

    public UIMenuAdmin(ServiceManager serviceManager, ReportManager reportManager, TaxiImplDAO taxiDAO) {
        this.serviceManager = serviceManager;
        this.reportManager = reportManager;
        this.taxiDAO = taxiDAO;

        // Basic configuration of the main administration window
        setTitle("BCN Taxis - Administration Control Panel");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Visual header
        JPanel panelHeader = new JPanel();
        panelHeader.setBackground(new Color(45, 45, 48));
        JLabel lblTitle = new JLabel("FLEET MANAGEMENT & ANALYTICS");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        panelHeader.add(lblTitle);
        add(panelHeader, BorderLayout.NORTH);

        // Centralized button panel
        JPanel panelButtons = new JPanel(new GridLayout(3, 1, 15, 15));
        panelButtons.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JButton btnAddTaxi = new JButton("Add new taxi to the fleet");
        JButton btnViewMap = new JButton("View Coverage Map (Active Taxis)");
        JButton btnReports = new JButton("Display Reports and Statistics");

        // Stylize buttons
        Font btnFont = new Font("Arial", Font.PLAIN, 14);
        btnAddTaxi.setFont(btnFont);
        btnViewMap.setFont(btnFont);
        btnReports.setFont(btnFont);

        // Swing Action Handlers
        btnAddTaxi.addActionListener(e -> openTaxiForm());
        btnViewMap.addActionListener(e -> showFleetMap());
        btnReports.addActionListener(e -> interceptAndShowReport());

        panelButtons.add(btnAddTaxi);
        panelButtons.add(btnViewMap);
        panelButtons.add(btnReports);

        add(panelButtons, BorderLayout.CENTER);
    }

    /**
     * Option 1: Interactive form to register a driver and their vehicle in SQLite
     */
    private void openTaxiForm() {
        // 1. Taxi Components
        JTextField txtLicensePlate = new JTextField(10);
        JTextField txtColor = new JTextField("Black/Yellow", 10);
        JTextField txtCapacity = new JTextField("4", 10);
        JComboBox<TaxiType> comboType = new JComboBox<>(TaxiType.values());

        // 2. Driver Components
        JTextField txtFirstNameDriver = new JTextField(10);
        JTextField txtLastNameDriver = new JTextField(10);
        JTextField txtDniDriver = new JTextField(10);
        JTextField txtTaxiLicenseDriver = new JTextField(10);
        JTextField txtAgeDriver = new JTextField("30", 10); // Required field in Driver table

        // 3. Location Components
        JTextField txtLat = new JTextField("41.3851", 10);
        JTextField txtLon = new JTextField("2.1734", 10);

        // Clean design with independent rows to avoid UI misalignment
        JPanel panelMain = new JPanel();
        panelMain.setLayout(new BoxLayout(panelMain, BoxLayout.Y_AXIS));

        // Tools for adding symmetrical sections and rows
        panelMain.add(createSectionSeparator("--- TAXI DETAILS ---"));
        panelMain.add(createFormRow("Car license:", txtLicensePlate, "Color:", txtColor));
        panelMain.add(createFormRow("Capacity:", txtCapacity, "Type:", comboType));

        panelMain.add(Box.createVerticalStrut(10));
        panelMain.add(createSectionSeparator("--- DRIVER DETAILS ---"));
        panelMain.add(createFormRow("Name:", txtFirstNameDriver, "Lastname:", txtLastNameDriver));
        panelMain.add(createFormRow("DNI:", txtDniDriver, "Age:", txtAgeDriver));
        panelMain.add(createFormRow("Driver license:", txtTaxiLicenseDriver, "", new JPanel()));

        panelMain.add(Box.createVerticalStrut(10));
        panelMain.add(createSectionSeparator("--- INITIAL GPS LOCATION ---"));
        panelMain.add(createFormRow("Latitude:", txtLat, "Longitude:", txtLon));

        int result = JOptionPane.showConfirmDialog(this, panelMain,
                "Vehicle and driver registration", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                // == STEP A: Create and Persist the Driver First ==
                Driver driver = new Driver();
                driver.setFirstName(txtFirstNameDriver.getText().trim());
                driver.setLastName(txtLastNameDriver.getText().trim());
                driver.setNationalId(txtDniDriver.getText().trim().toUpperCase());
                driver.setTaxiLicense(txtTaxiLicenseDriver.getText().trim());
                driver.setAge(Integer.parseInt(txtAgeDriver.getText().trim()));

                // Instantiate the DriverDAO using the database connection
                DriverImplDAO driverDAO = new DriverImplDAO(DatabaseConnection.getConnection());

                // Store the driver in SQLite and retrieve the auto-generated ID
                int generatedDriverId = driverDAO.createDriver(driver);
                driver.setIdDriver(generatedDriverId); // Assign the ID to your object model

                // == STEP B: Create Geographic Location ==
                Position initialPosition = new Position();
                initialPosition.setLatitude(Double.parseDouble(txtLat.getText().trim()));
                initialPosition.setLongitude(Double.parseDouble(txtLon.getText().trim()));

                // == STEP C: Create and persist the taxi by linking the driver ID ==
                Taxi newTaxi = new Taxi();
                newTaxi.setLicensePlate(txtLicensePlate.getText().trim().toUpperCase());
                newTaxi.setColor(txtColor.getText().trim());
                newTaxi.setCapacity(Integer.parseInt(txtCapacity.getText().trim()));
                newTaxi.setDriver(driver); // The object holds the complete nested driver
                newTaxi.setPosition(initialPosition);
                newTaxi.setType((TaxiType) comboType.getSelectedItem());
                newTaxi.setStatus(TaxiStatus.AVAILABLE);

                // Save the taxi in SQLite using your interface method
                int generatedTaxiId = taxiDAO.createTaxi(newTaxi);
                newTaxi.setIdTaxi(generatedTaxiId); // Set the ID assigned by the database

                // == STEP D: Synchronize the system's volatile memory ==
                serviceManager.getTaxis().add(newTaxi);

                JOptionPane.showMessageDialog(this, "Success! Driver saved with ID: "
                                + generatedDriverId + "\nTaxi registered with ID: " + generatedTaxiId,
                        "Registration Completed", JOptionPane.INFORMATION_MESSAGE);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Error: Capacity, Age, Latitude, and Longitude must be valid numerical values.",
                        "Format Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database Error while saving data: " + ex.getMessage(),
                        "SQL Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Option 2: Captures the standard console output from printReport() and displays it in a JTextArea
     */
    private void interceptAndShowReport() {
        // Temporarily redirect the standard System.out stream to an in-memory buffer
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream oldConsole = System.out;
        System.setOut(new PrintStream(buffer));

        // Execute the original statistical reporting method from your ReportManager
        reportManager.printReport(serviceManager.getActiveServices(), serviceManager.getWaitingList());

        // Restore the system console immediately
        System.setOut(oldConsole);

        // Dump the captured text into a scrollable window pane
        JTextArea txtReport = new JTextArea(22, 45);
        txtReport.setText(buffer.toString());
        txtReport.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Keeps the alignment frames stable
        txtReport.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(txtReport);

        JOptionPane.showMessageDialog(this, scrollPane, "Operational Performance Report", JOptionPane.PLAIN_MESSAGE);
    }

    private void showFleetMap() {
        JFrame mapFrame = new JFrame("Global Fleet Coverage Map");
        JXMapViewer mapViewer = new JXMapViewer();

        TileFactoryInfo info = new OSMTileFactoryInfo("OpenStreetMap", "https://tile.openstreetmap.org") {
            @Override
            public String getTileUrl(int x, int y, int zoom) {
                return super.getTileUrl(x, y, zoom);
            }
        };

        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        tileFactory.setThreadPoolSize(4);
        
        mapViewer.setTileFactory(tileFactory);

        System.setProperty("http.agent", "BCNTaxis/1.0 (educational project)");

        Set<DefaultWaypoint> waypoints = new HashSet<>();
        ArrayList<Taxi> taxiList = serviceManager.getTaxis();

        for (Taxi t : taxiList) {
            if (t.getPosition() != null) {
                GeoPosition geoPos = new GeoPosition(
                        t.getPosition().getLatitude(),
                        t.getPosition().getLongitude()
                );
                waypoints.add(new DefaultWaypoint(geoPos));
            }
        }

        ArrayList<Painter<JXMapViewer>> painters = new ArrayList<>();

        for (DefaultWaypoint wp : waypoints) {
            GeoPosition pos = wp.getPosition();
            painters.add((g, map, w, h) -> {
                Point2D point = map.convertGeoPositionToPoint(pos);
                int x = (int) point.getX();
                int y = (int) point.getY();

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int radio = 18;
                g2.setColor(Color.BLACK);
                g2.fillOval(x - (radio / 2), y - (radio / 2), radio, radio);
                g2.setColor(Color.RED);
                g2.fillOval(x - ((radio - 4) / 2), y - ((radio - 4) / 2), radio - 4, radio - 4);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 10));
                g2.drawString("T", x - 3, y + 4);

                g2.dispose();
            });
        }

        CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter<>(painters);
        mapViewer.setOverlayPainter(compoundPainter);



        mapViewer.setAddressLocation(new GeoPosition(41.3891, 2.1753));
        mapViewer.setZoom(3);

        mapFrame.add(mapViewer);
        mapFrame.setSize(1000, 700);
        mapFrame.setLocationRelativeTo(this);
        mapFrame.setVisible(true);

        mapViewer.revalidate();
        mapViewer.repaint();

    }

    private JPanel createFormRow(String label1, Component comp1, String label2, Component comp2) {
        JPanel panelRow = new JPanel(new GridLayout(1, 4, 8, 5));
        panelRow.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        panelRow.add(new JLabel(label1, SwingConstants.RIGHT));
        panelRow.add(comp1);
        panelRow.add(new JLabel(label2, SwingConstants.RIGHT));
        panelRow.add(comp2);
        return panelRow;
    }

    /**
     * Helper method to generate section headings that span across the window layout width
     */
    private JPanel createSectionSeparator(String text) {
        JPanel panelSep = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(new Color(70, 70, 70));
        panelSep.add(label);
        return panelSep;
    }
}