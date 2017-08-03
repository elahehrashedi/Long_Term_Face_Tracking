/*
 * MATLAB Compiler: 6.1 (R2015b)
 * Date: Thu May 05 21:02:57 2016
 * Arguments: "-B" "macro_default" "-W" "java:VideoTrackingLib22,VideoTrackClass" "-T" 
 * "link:lib" "-d" 
 * "C:\\Demo\\matconvnet-1.0-beta18\\Prj_Demo_Ver.1\\VideoTrackingLib22\\for_testing" 
 * "class{VideoTrackClass:C:\\Demo\\matconvnet-1.0-beta18\\Prj_Demo_Ver.1\\VTWVerification.m}" 
 */

package VideoTrackingLib22;

import com.mathworks.toolbox.javabuilder.*;
import com.mathworks.toolbox.javabuilder.internal.*;

/**
 * <i>INTERNAL USE ONLY</i>
 */
public class VideoTrackingLib22MCRFactory
{
   
    
    /** Component's uuid */
    private static final String sComponentId = "VideoTrackin_316D0B8EDE13C58E0092229AF2CAFB59";
    
    /** Component name */
    private static final String sComponentName = "VideoTrackingLib22";
    
   
    /** Pointer to default component options */
    private static final MWComponentOptions sDefaultComponentOptions = 
        new MWComponentOptions(
            MWCtfExtractLocation.EXTRACT_TO_CACHE, 
            new MWCtfClassLoaderSource(VideoTrackingLib22MCRFactory.class)
        );
    
    
    private VideoTrackingLib22MCRFactory()
    {
        // Never called.
    }
    
    public static MWMCR newInstance(MWComponentOptions componentOptions) throws MWException
    {
        if (null == componentOptions.getCtfSource()) {
            componentOptions = new MWComponentOptions(componentOptions);
            componentOptions.setCtfSource(sDefaultComponentOptions.getCtfSource());
        }
        return MWMCR.newInstance(
            componentOptions, 
            VideoTrackingLib22MCRFactory.class, 
            sComponentName, 
            sComponentId,
            new int[]{9,0,0}
        );
    }
    
    public static MWMCR newInstance() throws MWException
    {
        return newInstance(sDefaultComponentOptions);
    }
}
