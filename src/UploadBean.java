/**
 * Created with IntelliJ IDEA.
 * User: sergio
 * Date: 29/05/13
 * Time: 06:02 PM
 * To change this template use File | Settings | File Templates.
 */

import org.icefaces.ace.component.fileentry.FileEntry;
import org.icefaces.ace.component.fileentry.FileEntryEvent;
import org.icefaces.ace.component.fileentry.FileEntryResults;
import org.sg.recognition.Algorithm;
import org.sg.recognition.CVSParser;
import org.sg.recognition.exceptions.FilterException;
import org.sg.recognition.filters.BinaryFilter;
import org.sg.recognition.filters.IntegerFilter;
import org.sg.recognition.filters.NormalizeFilter;
import org.sg.recognition.utils.Pattern;
import org.sg.recognition.validations.KFoldCrossValidation;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.File;
import java.io.Serializable;
import java.util.List;

@ManagedBean(name= UploadBean.BEAN_NAME)
@SessionScoped
public class UploadBean implements Serializable {

    public static final String BEAN_NAME = "fileEntry";
    private String result = "0%";

    public UploadBean()
    {
    }

    @PostConstruct
    public void initMetaData() {
    }

    public void sampleListener(FileEntryEvent e)
    {
        FileEntry fe = (FileEntry)e.getComponent();
        FileEntryResults results = fe.getResults();
        File parent = null;


        //get data About File

        for (FileEntryResults.FileInfo i : results.getFiles())
        {

            if (i.isSaved()) {

                File file = i.getFile();
                if (file != null) {
                    parent = file.getParentFile();
                }
            } else {
                result = "File was not saved because: " + i.getStatus().getFacesMessage(FacesContext.getCurrentInstance(), fe, i).getSummary();
            }
        }

        if (parent != null) {
            long dirSize = 0;
            int fileCount = 0;
            for (File file : parent.listFiles()) {
                fileCount ++;
                dirSize += file.length();

                CVSParser parser = new CVSParser(file.getAbsolutePath());
                parser.setIgnoreHeader(true);
                parser.parse();
                List<Pattern> patterns = parser.getPatterns();

	            Algorithm mabm = new MAABmax();

	            NormalizeFilter normalizeFilter = new NormalizeFilter();
	            IntegerFilter integerFilter = new IntegerFilter();
	            BinaryFilter binaryFilter = new BinaryFilter();
	            try {
		            List<Pattern> normalized = normalizeFilter.filterList(patterns);
		            List<Pattern> integered = integerFilter.filterList(normalized);
		            List<Pattern> binarized = binaryFilter.filterList(integered);

		            mabm.setPatterns(binarized);
		            mabm.setValidationMethod(new KFoldCrossValidation(10));
		            mabm.run();

		            System.out.println(mabm.getPerformance());
	            } catch (FilterException ex) {
		            ex.printStackTrace();
	            }

                System.out.println(mabm.getPerformance());
                result = mabm.getPerformance() + "%";
            }
        }
    }

    public String getResult() {
        return result;
    }

}