import org.sg.recognition.*;
import org.sg.recognition.CVSParser;
import org.sg.recognition.exceptions.FilterException;
import org.sg.recognition.filters.BinaryFilter;
import org.sg.recognition.filters.IntegerFilter;
import org.sg.recognition.filters.NormalizeFilter;
import org.sg.recognition.utils.Pattern;
import org.sg.recognition.validations.KFoldCrossValidation;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sergio
 * Date: 28/05/13
 * Time: 07:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main {

    public Main() {
        CVSParser parser = new CVSParser(".csv");
	    parser.setIgnoreHeader(true);
	    parser.parse();
	    List<Pattern> patterns = parser.getPatterns();

	    Algorithm mam = new MAMmax();
	    mam.setPatterns(patterns);
	    mam.setValidationMethod(new KFoldCrossValidation(10));
	    mam.run();

	    System.out.println(mam.getPerformance());

        NormalizeFilter normalizeFilter = new NormalizeFilter();
        IntegerFilter integerFilter = new IntegerFilter();
        BinaryFilter binaryFilter = new BinaryFilter();
        try {
            List<Pattern> normalized = normalizeFilter.filterList(patterns);
            List<Pattern> integered = integerFilter.filterList(normalized);
            List<Pattern> binarized = binaryFilter.filterList(integered);

            Algorithm mabm = new MAABmax();
            mabm.setPatterns(binarized);
            mabm.setValidationMethod(new KFoldCrossValidation(10));
            mabm.run();

            System.out.println(mabm.getPerformance());
        } catch (FilterException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Main();
    }
}
