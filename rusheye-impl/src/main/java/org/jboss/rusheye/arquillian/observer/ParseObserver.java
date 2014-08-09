package org.jboss.rusheye.arquillian.observer;

import com.beust.jcommander.IStringConverter;
import java.io.File;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.rusheye.arquillian.configuration.RusheyeConfiguration;
import org.jboss.rusheye.arquillian.event.StartParsingEvent;
import org.jboss.rusheye.internal.Instantiator;
import org.jboss.rusheye.listener.SuiteListener;
import org.jboss.rusheye.parser.Parser;
import org.jboss.rusheye.suite.Properties;

/**
 *
 * @author jhuska
 */
public class ParseObserver {

    @Inject
    private Instance<RusheyeConfiguration> rusheyeConfiguration;

    private Properties properties = new Properties();
    private final SuiteListenerConverter suiteListenerConverter = new SuiteListenerConverter();

    public void parse(@Observes StartParsingEvent event) {
        initialize(event);
        
        Parser parser = new Parser();
        parser.setProperties(properties);
        
        if (rusheyeConfiguration.get().getSuiteListener() != null) {
            SuiteListener converted = suiteListenerConverter.convert(rusheyeConfiguration.get().getSuiteListener());
            parser.registerListener(converted);
        }
        
        File suiteDescriptor = new File(event.getPatternAndDescriptorFolder() + 
                File.separator + 
                rusheyeConfiguration.get().getSuiteDescriptor());
        parser.parseFile(suiteDescriptor);
    }

    public void initialize(StartParsingEvent event) {
        RusheyeConfiguration conf = rusheyeConfiguration.get();
        properties.setProperty("result-output-file", conf.getResultOutputFile());
        properties.setProperty("samples-directory", event.getSamplesFolder());
        properties.setProperty("patterns-directory", event.getPatternAndDescriptorFolder() 
                + File.separator + event.getSamplesFolder());
        properties.setProperty("file-storage-directory", conf.getDiffsDir());
    }

    public class SuiteListenerConverter implements IStringConverter<SuiteListener> {

        @Override
        public SuiteListener convert(String type) {
            return new Instantiator<SuiteListener>().getInstance(type);
        }
    }

}
