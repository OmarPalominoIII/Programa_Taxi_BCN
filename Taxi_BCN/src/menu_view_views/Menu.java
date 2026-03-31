package menu_view_views;
import logic.services.*;

public class Menu {
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
