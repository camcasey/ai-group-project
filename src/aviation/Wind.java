package aviation;

import coordinates.GeoCoordinate;

public class Wind {

    private final int heading;
    private final double speed;
    private final GeoCoordinate location;
    private double density;

    public Wind(int head, double speed, GeoCoordinate loc){
        this.heading = head;
        this.speed = speed;
        this.location = loc;
    }

    public double getAirDensity(double h){
        // given an altitude in m, returns density in kg/m^3

        // Setting up constants
        double a1 = -6.5*1e-3; // T-gradient for troposhere [K/m]
        double a3 = 3*1e-3;    // T-gradient for stratosphere [K/m]
        double a5 = -4.5*1e-3; // T-gradient for mesosphere [K/m]
        double a7 = 4.0*1e-3;  // T-gradient for thermosphere [K/m]

        double g = 9.80665;    // gravitational constant [m/s^2]
        double R = 286.9;      // specific gas constant for air [J/kgK]

        double T1i = 288.16;   // [K] temperature at sea level
        double rho1i = 1.225;  // [kg/m^3] density at sea level
        double h1i = 0;        // [m] sea level initial height

        // Manage which part of atmosphere

        if (h >= 0 && h <= 11000) { // Troposphere (Gradient w/ a1)
            double T = T1i + a1*(h - h1i); // Temperature at h in troposphere
            density =  rho1i * Math.pow((T / T1i),(-g / (a1 * R) - 1)); // Density at h in troposphere
        }
        else if ((h > 11000) && (h <= 25000)) {  // Tropopause (Isothermal)
            // Initial tropopause conditions
            double T2i = 216.66;       // [K]from T eq at 11000 m
            double rho2i = 0.3636;     // [kg / m ^ 3]from rho eq at 11000 m
            density = rho2i * Math.exp(-g / (R * T2i) * (h - 11000));
        }

        else if (h > 25000 && h <= 47000) { // Stratosphere (Gradient w/ a3)
            // Initial stratosphere conditions
            double T3i = 216.66;       // [K]from Tf of previous layer
            double rho3i = 0.0399;     // [kg / m ^ 3]from rho eq at 25000 m
            double h3i = 25000;

            double T = T3i + a3*(h - h3i);
            density = Math.pow(rho3i * (T / T3i),(-g / (a3 * R) - 1));
        }

        else if (h > 47000 && h <= 53000) {// Stratopause (Isothermal)
            // Initial stratopause conditions
            double T4i = 282.66;       // [K]from T eq at 47000 m
            double rho4i = 0.001479581102;     // [kg / m ^ 3]from rho eq at 47000 m

            density = rho4i * Math.exp(-g / (R * T4i) * (h - 47000));
        }

        else if (h > 53000 && h <= 79000) { // Mesosphere (Gradient w/ a5)
            // Initial mesosphere conditions
            double T5i = 282.66;       // [K]from Tf of previous layer
            double rho5i = 0.0007161918809;     // [kg / m ^ 3]from rho eq at 53000 m
            double h5i = 53000;

            double T = T5i + a5 * (h - h5i);
            density = rho5i * Math.pow((T / T5i),(-g / (a5 * R) - 1));
        }

        else if (h > 79000 && h <= 90000) {// Mesopause (Isothermal)
            // Initial mesopause conditions
            double T6i = 165.66;       // [K] from T eq at 79000 m
            double rho6i = 0.00002110955975;     // [kg/m^3] from rho eq at 79000 m

            density = rho6i * Math.exp(-g / (R * T6i) * (h - 79000));
        }

        else if (h > 90000 && h <= 100000) { // Thermosphere (Gradient w/ a7)
            // Initial thermosphere conditions
            double T7i = 165.66;       // [K]from Tf of previous layer
            double rho7i = 0.000002181566648;     // [kg / m ^ 3]from rho eq at 90000 m
            double h7i = 90000;

            double T = T7i + a7 * (h - h7i);
            density = Math.pow(rho7i * (T / T7i), (-g / (a7 * R) - 1));
        }
        return density;
    }

}
