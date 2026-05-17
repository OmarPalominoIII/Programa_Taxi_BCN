package user_interface.ui_reports;
import logic_services.*;
import project_models.ConsoleMessenger;
import project_models.Sendable;
import report_services.ReportManager;

public class LogicMenuReports {
    private ServiceManager serviceManager;
    private ReportManager reportManager;
    private Sendable messenger;

    public LogicMenuReports(){
        this.reportManager = new ReportManager();
        this.messenger = new ConsoleMessenger();
        this.serviceManager = new ServiceManager(this.reportManager, this.messenger);
    }


    public static String imprimirOpciones(){
        return
                """
                        
                        --- Menú Gestió Serveis ---\
                        
                        1. Crear servei\
                        
                        2. Marcar arribada del taxi\
                        
                        3. Finalitzar servei\
                        
                        4. Mostrar serveis\
                        
                        5. Sortir\
                        
                        Opció:\s""";
    }
}
