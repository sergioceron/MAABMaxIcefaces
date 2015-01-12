import org.sg.recognition.Algorithm;
import org.sg.recognition.ValidationMethod;
import org.sg.recognition.utils.Matrix;
import org.sg.recognition.utils.Pattern;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sergio
 * Date: 28/05/13
 * Time: 07:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class MAMmax extends Algorithm {

    public MAMmax() {
        super("Memoria Morfologica Autoasociativa MAX");
    }

    @Override
    public void run() {
        ValidationMethod vm = getValidationMethod();
        vm.setPatterns(getPatterns());
        vm.validate();
        List<Pattern>[] trains = vm.trainingSet();
        List<Pattern>[] tests  = vm.testSet();

        double performance = 0;
        for (int i = 0; i < trains.length; i++) {
            List<Pattern> train = trains[i];
            List<Pattern> test  = tests[i];

            int n = train.get(0).size();
            int m = train.get(0).size();
            int p = train.size();

            // Entrenamiento
            Double[][] M = Matrix.fill(m, n, -Double.MAX_VALUE);
            for ( int t = 0; t < p; t++ ) {
                Pattern<Double> pattern  = train.get(t);
                Double[] xM = pattern.getFeaturesAsVector();
                Double[] yM = pattern.getFeaturesAsVector();
                Double[][] zM = Y(xM, yM);
                M = Matrix.max(zM, M);
            }

            double partialPeformance = 0;
            // Recuperacion
            for ( int t = 0; t < test.size(); t++ ) {
                Pattern<Double> pattern  = test.get(t);
                Double[] xM  = pattern.getFeaturesAsVector();
                Double[] yM  = pattern.getFeaturesAsVector();

                Pattern<Double> recuperado = new Pattern<Double>( Z(M, xM) );
                Pattern<Double> closest = closestPattern(train, recuperado);

                if ( pattern.getClase() == closest.getClase() ) {
                    partialPeformance += 1;
                }

                /*if ( Matrix.equals(yM, yM_) ) {
                    partialPeformance += 1;
                }*/
            }

            partialPeformance = partialPeformance / test.size();
            performance += partialPeformance;
        }
        performance = performance / trains.length;
        setPerformance(performance);
    }

    public Double[][] Y(Double[] xM, Double[] yM) {
        Double[][] zM = Matrix.fill(yM.length, xM.length, 0d);
        for (int i = 0; i < yM.length; i++) {
            for (int j = 0; j < xM.length; j++) {
                zM[i][j] = A(yM[i], xM[j]);
            }
        }
        return zM;
    }

    public Double[] Z(Double[][] M, Double[] xM) {
        int m1rows = M.length;
        int m1cols = M[0].length;

        Double[] result = new Double[m1rows];

        for (int i = 0; i < m1rows; i++) {
            Double _min = Double.MAX_VALUE;
            for (int k = 0; k < m1cols; k++) {
                Double b = B(M[i][k], xM[k]);
                _min = b < _min ? b : _min;
            }
            result[i] = _min;
        }

        return result;
    }

    public Double A(Double x, Double y){
        return round(x - y);
    }

    public Double B(Double x, Double y){
        return round(x + y);
    }

    public Pattern<Double> closestPattern(List<Pattern> lookupTable, Pattern<Double> other){
        double distmin = Double.MAX_VALUE;
        Pattern<Double> closest = null;
        for ( Pattern<Double> pattern : lookupTable ) {
            double dist = Matrix.distance(pattern.getFeaturesAsVector(), other.getFeaturesAsVector());
            if ( dist < distmin ) {
                closest = pattern;
                distmin = dist;
            }
        }
        return closest;
    }

    private Double round(Double value){
        return (double) Math.round(value * 100) / 100;
    }
}
