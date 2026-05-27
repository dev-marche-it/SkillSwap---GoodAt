package it.skillswap.app;

/**
 * Banner ASCII mostrato all'avvio della CLI.
 */
public final class ConsoleBanner {

    private ConsoleBanner() {}

    public static void print() {
        System.out.println();
        System.out.println(" _______  ___   _  ___   ___      ___        _______  _     _  _______  _______ ");
        System.out.println("|       ||   | | ||   | |   |    |   |      |       || | _ | ||   _   ||       |");
        System.out.println("|  _____||   |_| ||   | |   |    |   |      |  _____|| || || ||  |_|  ||    _  |");
        System.out.println("| |_____ |      _||   | |   |    |   |      | |_____ |       ||       ||   |_| |");
        System.out.println("|_____  ||     |_ |   | |   |___ |   |___   |_____  ||       ||       ||    ___|");
        System.out.println(" _____| ||    _  ||   | |       ||       |   _____| ||   _   ||   _   ||   |    ");
        System.out.println("|_______||___| |_||___| |_______||_______|  |_______||__| |__||__| |__||___|    ");
        System.out.println();
        System.out.println("  SkillSwap School — scambio competenze tra studenti");
        System.out.println();
    }
}
