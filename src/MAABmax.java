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
public class MAABmax extends Algorithm {

    public MAABmax() {
        super("Memoria Alfa Beta Autoasociativa MAX");
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
            Integer[][] M = Matrix.fill(m, n, -Integer.MAX_VALUE);
            for ( int t = 0; t < p; t++ ) {
                Pattern<Integer> pattern  = train.get(t);
                Integer[] xM = pattern.getFeaturesAsVector();
                Integer[] yM = pattern.getFeaturesAsVector();
                Integer[][] zM = Y(xM, yM);
                M = Matrix.max(zM, M);
            }

            double partialPeformance = 0;
            // Recuperacion
            for ( int t = 0; t < test.size(); t++ ) {
                Pattern<Integer> pattern  = test.get(t);
                Integer[] xM  = pattern.getFeaturesAsVector();
                Integer[] yM  = pattern.getFeaturesAsVector();

                Pattern<Integer> recuperado = new Pattern<Integer>( Z(M, xM) );
                Pattern<Integer> closest = closestPattern(train, recuperado);

                if ( pattern.getClase() == closest.getClase() ) {
                    partialPeformance += 1;
                }

            }

            partialPeformance = partialPeformance / test.size();
            performance += partialPeformance;
        }
        performance = performance / trains.length;
        setPerformance(performance);
    }

    public Integer[][] Y(Integer[] xM, Integer[] yM) {
        Integer[][] zM = Matrix.fill(yM.length, xM.length, 0);
        for (int i = 0; i < yM.length; i++) {
            for (int j = 0; j < xM.length; j++) {
                zM[i][j] = A(yM[i], xM[j]);
            }
        }
        return zM;
    }

    public Integer[] Z(Integer[][] M, Integer[] xM) {
        int m1rows = M.length;
        int m1cols = M[0].length;

        Integer[] result = new Integer[m1rows];

        for (int i = 0; i < m1rows; i++) {
            int _min = 2;
            for (int k = 0; k < m1cols; k++) {
                int b = B(M[i][k], xM[k]);
                _min = b < _min ? b : _min;
            }
            result[i] = _min;
        }

        return result;
    }

    public Integer A(Integer x, Integer y){
        int z = 0;
        if (x == 0 && y == 0) {
            z = 1;
        } else if (x == 0 && y == 1) {
            z = 0;
        } else if (x == 1 && y == 0) {
            z = 2;
        } else if (x == 1 && y == 1) {
            z = 1;
        }
        return z;
    }

    public Integer B(Integer x, Integer y){
        int z = 0;
        if (x == 0 && y == 0 ) {
            z = 0;
        } else if (x == 0 && y == 1) {
            z = 0;
        } else if (x == 1 && y == 0) {
            z = 0;
        } else if (x == 1 && y == 1) {
            z = 1;
        } else if (x == 2 && y == 0) {
            z = 1;
        } else if (x == 2 && y == 1) {
            z = 1;
        }
        return z;
    }

    public Pattern<Integer> closestPattern(List<Pattern> lookupTable, Pattern<Integer> other){
        double distmin = Double.MAX_VALUE;
        Pattern<Integer> closest = null;
        for ( Pattern<Integer> pattern : lookupTable ) {
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
