package VASL.build.module.map;

import VASL.build.module.ASLMap;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import org.w3c.dom.Element;
import VASSAL.Info;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.map.PieceMover;
import VASSAL.build.widget.PieceSlot;
import VASSAL.counters.DragBuffer;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyBuffer;
import VASSAL.counters.PieceCloner;
import VASSAL.counters.Properties;
import java.awt.Component;
import java.awt.Insets;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.io.FilenameFilter;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class QCStartMenuItem extends JPopupMenu.Separator
{}

class QCEndMenuItem extends JPopupMenu.Separator
{}

class QCStartToolBarItem extends JToolBar.Separator
{}

class QCEndToolBarItem extends JToolBar.Separator
{}

class QCRadioButtonMenuItem extends JRadioButtonMenuItem
// <editor-fold defaultstate="collapsed">
{
    private final QCConfiguration m_objQCConfiguration;
    
    QCRadioButtonMenuItem(QCConfiguration objConfiguration)
    {
        super(objConfiguration.getDescription());
        
        m_objQCConfiguration = objConfiguration;
    }

    /**
     * @return the m_objQCConfiguration
     */
    public QCConfiguration getQCConfiguration() {
        return m_objQCConfiguration;
    }
}
// </editor-fold>

class QCButton extends JButton
// <editor-fold defaultstate="collapsed">
{
    PieceSlot m_objPieceSlot;
        
    public QCButton(PieceSlot objPieceSlot) 
    {
        super();
        m_objPieceSlot = objPieceSlot;
    }
    
    public void InitDragDrop()
    {
        DragGestureListener dragGestureListener = new DragGestureListener() 
        {
            public void dragGestureRecognized(DragGestureEvent dge) 
            {
                startDrag();
                PieceMover.AbstractDragHandler.getTheDragHandler().dragGestureRecognized(dge);
            }
        };
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, dragGestureListener);
    }
    
    // Puts counter in DragBuffer. Call when mouse gesture recognized
    protected void startDrag() 
    {
        if (m_objPieceSlot != null)
        {
            m_objPieceSlot.getPiece().setPosition(new Point(0, 0));

            // Erase selection border to avoid leaving selected after mouse dragged out
            m_objPieceSlot.getPiece().setProperty(Properties.SELECTED, null);

            if (m_objPieceSlot.getPiece() != null) {
                KeyBuffer.getBuffer().clear();
                DragBuffer.getBuffer().clear();
                GamePiece l_objNewPiece = PieceCloner.getInstance().clonePiece(m_objPieceSlot.getPiece());
                l_objNewPiece.setProperty(Properties.PIECE_ID, m_objPieceSlot.getGpId());
                DragBuffer.getBuffer().add(l_objNewPiece);
            }
        }
    }
}
// </editor-fold>

class QCButtonMenu extends JButton
// <editor-fold defaultstate="collapsed">
{
    private PieceSlot m_objPieceSlot;
    private JPopupMenu m_objPopupMenu = new JPopupMenu();
        
    public QCButtonMenu(PieceSlot objPieceSlot) 
    {
        super("M");
        m_objPieceSlot = objPieceSlot;
        
        addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e)
            {
                if (getPopupMenu() != null)
                    if (e.getSource() instanceof JButton)
                        getPopupMenu().show((JButton)e.getSource(), ((JButton)e.getSource()).getWidth() - 5, ((JButton)e.getSource()).getHeight() - 5);
            }
        });      
    }    

    /**
     * @return the m_objPopupMenu
     */
    public JPopupMenu getPopupMenu() {
        return m_objPopupMenu;
    }
}
// </editor-fold>

class QCMenuItem extends JMenuItem implements DragSourceListener
// <editor-fold defaultstate="collapsed">
{
    PieceSlot m_objPieceSlot;
    
    public QCMenuItem(PieceSlot objPieceSlot) 
    {
        super();
        m_objPieceSlot = objPieceSlot;
    }
    
    public void InitDragDrop()
    {
        DragGestureListener dragGestureListener = new DragGestureListener() 
        {
            public void dragGestureRecognized(DragGestureEvent dge) 
            {
                if ((dge.getComponent() != null) && (dge.getComponent() instanceof QCMenuItem))
                    DragSource.getDefaultDragSource().addDragSourceListener(((QCMenuItem)dge.getComponent()));
                
                startDrag();
                PieceMover.AbstractDragHandler.getTheDragHandler().dragGestureRecognized(dge);
            }
        };
        
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, dragGestureListener);
    }
    
    // Puts counter in DragBuffer. Call when mouse gesture recognized
    protected void startDrag() 
    {
        if (m_objPieceSlot != null)
        {
            m_objPieceSlot.getPiece().setPosition(new Point(0, 0));

            // Erase selection border to avoid leaving selected after mouse dragged out
            m_objPieceSlot.getPiece().setProperty(Properties.SELECTED, null);

            if (m_objPieceSlot.getPiece() != null) 
            {
                KeyBuffer.getBuffer().clear();
                DragBuffer.getBuffer().clear();
                GamePiece l_objNewPiece = PieceCloner.getInstance().clonePiece(m_objPieceSlot.getPiece());
                l_objNewPiece.setProperty(Properties.PIECE_ID, m_objPieceSlot.getGpId());
                DragBuffer.getBuffer().add(l_objNewPiece);
            }
        }
    }

    public void dragEnter(DragSourceDragEvent dsde) {
    }

    public void dragOver(DragSourceDragEvent dsde) {
    }

    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    public void dragExit(DragSourceEvent dse) {
    }

    public void dragDropEnd(DragSourceDropEvent dsde) {
        if (getParent() != null)
            if (getParent() instanceof JPopupMenu)
                ((JPopupMenu) getParent()).setVisible(false);
        
        DragSource.getDefaultDragSource().removeDragSourceListener(this);
    }
}
// </editor-fold>

class QCConfiguration extends DefaultHandler 
// <editor-fold defaultstate="collapsed">
{
    private QC m_objQC;
    private File m_objFile;
    private String m_strDescription;
    private ArrayList<QCConfigurationEntry> mar_objListConfigurationEntry;
    private QCConfigurationEntry m_objCurrentSubMenu;
    
    public QCConfiguration(QC objQC, File objFile)
    {
        m_objCurrentSubMenu = null;
        
        m_objQC = objQC;
        m_objFile = objFile;
        
        m_strDescription = "";
        mar_objListConfigurationEntry = new ArrayList<QCConfigurationEntry>();
    }    

    /**
     * @return the m_objQC
     */
    public QC getQC() {
        return m_objQC;
    }

    /**
     * @return the m_objFile
     */
    public File getFile() {
        return m_objFile;
    }

    /**
     * @return the namne of the file
     */
    public String getName() {
        if (m_objFile != null)
            return m_objFile.getName();
        else
            return "";
    }
    
    /**
     * @return the m_strDescription
     */
    public String getDescription() {
        return m_strDescription;
    }

    /**
     * @param m_strDescription the m_strDescription to set
     */
    public void setDescription(String strDescription) {
        this.m_strDescription = strDescription;
    }

    /**
     * @return the mar_objConfigurationEntry
     */
    public ArrayList<QCConfigurationEntry> getListConfigurationEntry() {
        return mar_objListConfigurationEntry;
    }
    
    @Override
    public void startElement (String uri, String localName,
                              String qName, Attributes attributes)
        throws SAXException
    {
        if (qName.equalsIgnoreCase("qcconfig")) 
        {
            setDescription(attributes.getValue("descr"));
        }
        else if (qName.equalsIgnoreCase("qcsubmenu")) 
        {
            QCConfigurationEntry l_newEntry = new QCConfigurationEntry(getQC());
            
            l_newEntry.setMenu(true);
            l_newEntry.setGpID(attributes.getValue("slot"));
            
            if (m_objCurrentSubMenu != null)
            {
                l_newEntry.setParent(m_objCurrentSubMenu);
                m_objCurrentSubMenu.getListConfigurationEntry().add(l_newEntry);
            }
            else
                mar_objListConfigurationEntry.add(l_newEntry);
            
            m_objCurrentSubMenu = l_newEntry;
        }
        else if (qName.equalsIgnoreCase("qcentry")) 
        {
            QCConfigurationEntry l_newEntry = new QCConfigurationEntry(getQC());

            l_newEntry.setGpID(attributes.getValue("slot"));
            
            if (m_objCurrentSubMenu != null)
                m_objCurrentSubMenu.getListConfigurationEntry().add(l_newEntry);
            else
                mar_objListConfigurationEntry.add(l_newEntry);            
        }
    }  
    
    @Override
    public void endElement (String uri, String localName, String qName)
        throws SAXException
    {
        if (qName.equalsIgnoreCase("qcsubmenu")) 
            m_objCurrentSubMenu = m_objCurrentSubMenu.getParent();
    }  
    
    public boolean SaveXML() 
    {
        try 
        {
            if (m_objFile != null)
            {
                if (!m_objFile.exists())
                    m_objFile.createNewFile();

                DocumentBuilderFactory l_objDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder l_objDocumentBuilder = l_objDocumentBuilderFactory.newDocumentBuilder();

                // root elements
                Document l_objDocument = l_objDocumentBuilder.newDocument();
                Element l_objRootElement = l_objDocument.createElement("qcconfig");

                Attr l_objAttributes = l_objDocument.createAttribute("descr");
                l_objAttributes.setValue(getDescription());
                l_objRootElement.setAttributeNode(l_objAttributes);

                l_objDocument.appendChild(l_objRootElement);

                for (QCConfigurationEntry l_objConfigurationEntry : mar_objListConfigurationEntry)
                    l_objConfigurationEntry.WriteXML(l_objDocument, l_objRootElement);

                // write the content into xml file
                TransformerFactory l_objTransformerFactory = TransformerFactory.newInstance();
                Transformer l_objTransformer = l_objTransformerFactory.newTransformer();
                DOMSource l_objDOMSource = new DOMSource(l_objDocument);

                l_objTransformer.transform(l_objDOMSource, new StreamResult(new FileOutputStream(m_objFile)));
            }
            
            return true;
 
        } 
        catch (Exception e) 
        {
        }

        return false;
    }
}
// </editor-fold>

class QCConfigurationEntry
// <editor-fold defaultstate="collapsed">
{
    private final QC m_objQC;
    private boolean m_bMenu;
    private ArrayList<QCConfigurationEntry> mar_objListConfigurationEntry;
    private String m_strGpID;    
    private QCConfigurationEntry m_objParentEntry;
    private PieceSlot m_objPieceSlot;
    
    public QCConfigurationEntry(QC objQC)
    {
        m_objQC = objQC;
        m_bMenu = false;
        mar_objListConfigurationEntry = null;
        m_strGpID = "";
        m_objParentEntry = null;
        m_objPieceSlot = null;
    }   

    /**
     * @return the m_bMenu
     */
    public boolean isMenu() {
        return m_bMenu;
    }

    /**
     * @param bMenu the bMenu to set
     */
    public void setMenu(boolean bMenu) {
        this.m_bMenu = bMenu;
        
        if (m_bMenu)
            mar_objListConfigurationEntry = new ArrayList<QCConfigurationEntry>();
    }

    /**
     * @return the mar_objListConfigurationEntry
     */
    public ArrayList<QCConfigurationEntry> getListConfigurationEntry() {
        return mar_objListConfigurationEntry;
    }

    /**
     * @return the m_strPieceSlot
     */
    public String getGpID() {
        return m_strGpID;
    }

    /**
     * @param strGpID the strGpID to set
     */
    public void setGpID(String strGpID) {
        this.m_strGpID = strGpID;
    }

    /**
     * @return the m_objQC
     */
    public QC getQC() {
        return m_objQC;
    }

    /**
     * @return the m_objParentEntry
     */
    public QCConfigurationEntry getParent() {
        return m_objParentEntry;
    }

    /**
     * @param objParentEntry the objParentEntry to set
     */
    public void setParent(QCConfigurationEntry objParentEntry) {
        this.m_objParentEntry = objParentEntry;
    }

    void WriteXML(Document objDocument, Element objElement) 
    {
        if (isMenu())
        {
            Element l_objEntry = objDocument.createElement("qcsubmenu");

            // set attribute to staff element
            Attr l_objAttributes = objDocument.createAttribute("slot");
            l_objAttributes.setValue(getGpID());
            l_objEntry.setAttributeNode(l_objAttributes);
            
            objElement.appendChild(l_objEntry);
            
            for (QCConfigurationEntry l_objConfigurationEntry : mar_objListConfigurationEntry)
                l_objConfigurationEntry.WriteXML(objDocument, l_objEntry);
        }
        else
        {
            Element l_objEntry = objDocument.createElement("qcentry");

            // set attribute to staff element
            Attr l_objAttributes = objDocument.createAttribute("slot");
            l_objAttributes.setValue(getGpID());
            l_objEntry.setAttributeNode(l_objAttributes);
            
            objElement.appendChild(l_objEntry);
        }
    }

    /**
     * @return the m_objPieceSlot
     */
    public PieceSlot getPieceSlot() {
        return m_objPieceSlot;
    }

    /**
     * @param objPieceSlot the objPieceSlot to set
     */
    public void setPieceSlot(PieceSlot objPieceSlot) {
        this.m_objPieceSlot = objPieceSlot;
    }
}
// </editor-fold>

/**
 * A class to represent the counters toolbar
 */
public class QC implements Buildable 
{
// <editor-fold defaultstate="collapsed">
    private final String mc_strDefaultConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<qcconfig descr=\"Default configuration\">\n" +
"	<qcentry slot=\"0\"/>\n" +
"	<qcentry slot=\"11\"/>\n" +
"	<qcentry slot=\"12\"/>\n" +
"	<qcentry slot=\"13\"/>\n" +
"	<qcentry slot=\"15\"/>\n" +
"	<qcentry slot=\"18\"/>\n" +
"	<qcentry slot=\"19\"/>\n" +
"	<qcentry slot=\"21\"/>\n" +
"	<qcentry slot=\"1\"/>\n" +
"	<qcentry slot=\"2\"/>\n" +
"	<qcentry slot=\"4\"/>\n" +
"	<qcentry slot=\"63\"/>\n" +
"	<qcsubmenu slot=\"47\">\n" +
"		<qcentry slot=\"47\"/>\n" +
"		<qcentry slot=\"48\"/>\n" +
"		<qcentry slot=\"49\"/>\n" +
"		<qcentry slot=\"50\"/>\n" +
"		<qcentry slot=\"51\"/>\n" +
"		<qcentry slot=\"52\"/>\n" +
"	</qcsubmenu>\n" +
"	<qcsubmenu slot=\"53\">\n" +
"		<qcentry slot=\"53\"/>\n" +
"		<qcentry slot=\"54\"/>\n" +
"		<qcentry slot=\"55\"/>\n" +
"		<qcentry slot=\"56\"/>\n" +
"	</qcsubmenu>\n" +
"	<qcsubmenu slot=\"6\">\n" +
"		<qcentry slot=\"6\"/>\n" +
"		<qcentry slot=\"57\"/>\n" +
"		<qcentry slot=\"59\"/>\n" +
"		<qcentry slot=\"61\"/>\n" +
"		<qcentry slot=\"72\"/>\n" +
"		<qcentry slot=\"58\"/>\n" +
"		<qcentry slot=\"60\"/>\n" +
"		<qcentry slot=\"62\"/>\n" +
"		<qcentry slot=\"114\"/>\n" +
"	</qcsubmenu>\n" +
"	<qcentry slot=\"69\"/>\n" +
"	<qcentry slot=\"146\"/>\n" +
"	<qcentry slot=\"64\"/>\n" +
"	<qcsubmenu slot=\"73\">\n" +
"		<qcentry slot=\"73\"/>\n" +
"		<qcentry slot=\"74\"/>\n" +
"		<qcentry slot=\"83\"/>\n" +
"		<qcentry slot=\"85\"/>\n" +
"		<qcentry slot=\"88\"/>\n" +
"	</qcsubmenu>\n" +
"	<qcsubmenu slot=\"6390\">\n" +
"		<qcentry slot=\"6390\"/>\n" +
"		<qcentry slot=\"6391\"/>\n" +
"	</qcsubmenu>\n" +
"	<qcsubmenu slot=\"68\">\n" +
"		<qcentry slot=\"68\"/>\n" +
"		<qcentry slot=\"66\"/>\n" +
"		<qcentry slot=\"5903\"/>\n" +
"		<qcentry slot=\"5902\"/>\n" +
"	</qcsubmenu>\n" +
"	<qcsubmenu slot=\"423\">\n" +
"		<qcentry slot=\"423\"/>\n" +
"		<qcentry slot=\"773\"/>\n" +
"		<qcentry slot=\"1066\"/>\n" +
"		<qcentry slot=\"1589\"/>\n" +
"		<qcentry slot=\"1939\"/>\n" +
"		<qcentry slot=\"2122\"/>\n" +
"		<qcentry slot=\"2353\"/>\n" +
"		<qcentry slot=\"2831\"/>\n" +
"		<qcentry slot=\"3192\"/>\n" +
"		<qcentry slot=\"3380\"/>\n" +
"		<qcentry slot=\"3573\"/>\n" +
"		<qcentry slot=\"3634\"/>\n" +
"		<qcentry slot=\"3916\"/>\n" +
"	</qcsubmenu>\n" +
"	<qcsubmenu slot=\"41\">\n" +
"		<qcentry slot=\"41\"/>\n" +
"		<qcentry slot=\"5918\"/>\n" +
"		<qcentry slot=\"5919\"/>\n" +
"		<qcentry slot=\"5920\"/>\n" +
"	</qcsubmenu>\n" +
"	<qcentry slot=\"101\"/>\n" +
"	<qcentry slot=\"109\"/>\n" +
"	<qcentry slot=\"111\"/>\n" +
"	<qcentry slot=\"113\"/>\n" +
"	<qcentry slot=\"126\"/>\n" +
"	<qcentry slot=\"128\"/>\n" +
"	<qcentry slot=\"129\"/>\n" +
"	<qcentry slot=\"116\"/>\n" +
"	<qcentry slot=\"123\"/>\n" +
"	<qcentry slot=\"104\"/>\n" +
"	<qcentry slot=\"344\"/>\n" +
"	<qcsubmenu slot=\"171\">\n" +
"		<qcentry slot=\"171\"/>\n" +
"		<qcentry slot=\"167\"/>\n" +
"		<qcentry slot=\"169\"/>\n" +
"		<qcentry slot=\"163\"/>\n" +
"		<qcentry slot=\"165\"/>\n" +
"		<qcentry slot=\"202\"/>\n" +
"		<qcentry slot=\"203\"/>\n" +
"		<qcentry slot=\"204\"/>\n" +
"	</qcsubmenu>\n" +
"	<qcsubmenu slot=\"359\">\n" +
"		<qcentry slot=\"359\"/>\n" +
"		<qcentry slot=\"360\"/>\n" +
"		<qcentry slot=\"361\"/>\n" +
"		<qcentry slot=\"362\"/>\n" +
"		<qcentry slot=\"363\"/>\n" +
"		<qcentry slot=\"364\"/>\n" +
"		<qcentry slot=\"365\"/>\n" +
"		<qcentry slot=\"366\"/>\n" +
"		<qcentry slot=\"367\"/>\n" +
"	</qcsubmenu>\n" +
"</qcconfig>";
// </editor-fold>

    private Map m_Map;
    private final ArrayList<QCConfiguration> mar_objListQCConfigurations = new ArrayList<QCConfiguration>();
    private QCConfiguration m_objQCWorkingConfiguration = null;
    private Timer m_objLinkTimer;

    public void loadConfigurations() 
    {
        SAXParser l_objXMLParser;
        Path l_pathConfigs = Paths.get(Info.getHomeDir() + System.getProperty("file.separator","\\") + "qcconfigs");
        
        try 
        {
            SAXParserFactory l_objXMLParserFactory = SAXParserFactory.newInstance();

            l_objXMLParser = l_objXMLParserFactory.newSAXParser();
        } 
        catch (Exception ex) 
        {
            l_objXMLParser = null;                    
        }

        if (l_objXMLParser != null)
        {
            // clear the old configurations (if any)
            mar_objListQCConfigurations.clear();
            
            // read default configuration
            m_objQCWorkingConfiguration = new QCConfiguration(this, null); // null file for the default configuration

            try 
            {
                // parse the default configuration
                l_objXMLParser.parse(new InputSource(new StringReader(mc_strDefaultConfig)), m_objQCWorkingConfiguration);
            }
            catch (Exception ex) 
            {
                m_objQCWorkingConfiguration = null;
            }

            if (m_objQCWorkingConfiguration != null)
                mar_objListQCConfigurations.add(m_objQCWorkingConfiguration);            
        
            // now read the custom configuration files
            // check for configs dir
            if (Files.notExists(l_pathConfigs)) 
            {
                try
                {
                    Files.createDirectory(l_pathConfigs);
                } 
                catch(Exception e)
                {
                }        
            }
            else // browsing configs files
            {
                File[] lar_objConfigFiles = l_pathConfigs.toFile().listFiles(new FilenameFilter() { public boolean accept(File objFile, String strName) 
                                                                                    {
                                                                                        return strName.toLowerCase().endsWith(".xml");
                                                                                    }});            
                for (File l_objConfigFile : lar_objConfigFiles) 
                {
                    //Create an instance of this class; it defines all the handler methods
                    QCConfiguration l_objQCConfiguration = new QCConfiguration(this, l_objConfigFile);

                    try 
                    {
                        // parse the config file
                        l_objXMLParser.parse(l_objConfigFile, l_objQCConfiguration);
                    }
                    catch (Exception ex) 
                    {
                        l_objQCConfiguration = null;
                    }

                    if (l_objQCConfiguration != null)
                        mar_objListQCConfigurations.add(l_objQCConfiguration);
                    else
                    {
                        try
                        {
                            l_objConfigFile.delete();
                        }
                        catch (Exception ex) 
                        {
                        }
                    }
                }
            }      
        }
    }
    public void readWorkingConfiguration() 
    {
        java.util.Properties l_objProperties = new java.util.Properties();
        InputStream l_objInputStream;
        String l_strWorkingConfigurationName = "";

        // try loading QC.properties from from the home directory
        try 
        {
            l_objInputStream = new FileInputStream(new File(Info.getHomeDir() + System.getProperty("file.separator","\\") + "QC.properties"));
        }
        catch (Exception ex) 
        { 
            l_objInputStream = null; 
        }
        
        if (l_objInputStream != null)
        {
            try 
            {
                l_objProperties.load(l_objInputStream);

                l_objInputStream.close();

                l_strWorkingConfigurationName = l_objProperties.getProperty("QC_working_configuration", "");
            }
            catch (Exception ex) 
            { 
            }
        }
        
        for (QCConfiguration l_objQCConfiguration : mar_objListQCConfigurations)
        {
            if (l_strWorkingConfigurationName.equalsIgnoreCase(l_objQCConfiguration.getName()))
            {
                m_objQCWorkingConfiguration = l_objQCConfiguration;
                break;                
            }
        }
    }	

    public void saveWorkingConfiguration() 
    {
        if (m_objQCWorkingConfiguration != null)
        {
            try 
            {
                java.util.Properties l_Props = new java.util.Properties();
                l_Props.setProperty("QC_working_configuration", m_objQCWorkingConfiguration.getName());

                OutputStream l_objOutputStream = new FileOutputStream(new File(Info.getHomeDir() + System.getProperty("file.separator","\\") + "QC.properties"));

                l_Props.store(l_objOutputStream, "QC working configuration");

                l_objOutputStream.flush();
                l_objOutputStream.close();
            }
            catch (Exception ex) 
            {
            }
        }
    }

    public void build(Element e)
    {
        loadConfigurations();
        readWorkingConfiguration();
    }

    public org.w3c.dom.Element getBuildElement(org.w3c.dom.Document doc) 
    {
        return doc.createElement(getClass().getName());
    }
  
    public void addTo(Buildable b) 
    {
        m_Map = (Map) b;
        
        ((ASLMap)m_Map).getPopupMenu().add(new QCStartMenuItem());
        ((ASLMap)m_Map).getPopupMenu().add(new QCEndMenuItem());
        
        RebuildPopupMenu();

        m_Map.getToolBar().add(new QCStartToolBarItem());
        m_Map.getToolBar().add(new QCEndToolBarItem());
        
        m_objLinkTimer = new Timer(5000, new ActionListener() 
        {
            public void actionPerformed(ActionEvent e) 
            {
                m_objLinkTimer.stop();
                
                ReadPiecesSlot();
                RebuildToolBar();
            }
        });
            
        m_objLinkTimer.setRepeats(false);
        m_objLinkTimer.start();
    }
    
    private void ReadPiecesSlot()
    {
        List<PieceSlot> lar_PieceSlotL = GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class);
        Hashtable lar_HashPieceSlot = new Hashtable();

        for (PieceSlot l_objPieceSlot : lar_PieceSlotL) 
            lar_HashPieceSlot.put(l_objPieceSlot.getGpId(), l_objPieceSlot);
        
        lar_PieceSlotL = null;
        
        for (QCConfiguration l_objQCConfiguration : mar_objListQCConfigurations)
        {
            for (QCConfigurationEntry l_objConfigurationEntry : l_objQCConfiguration.getListConfigurationEntry())
                setPieceSlot(l_objConfigurationEntry, lar_HashPieceSlot);
        }
    }
    
    private void setPieceSlot(QCConfigurationEntry objConfigurationEntry, Hashtable ar_HashPieceSlot) 
    {
        PieceSlot l_objPieceSlot = (PieceSlot) ar_HashPieceSlot.get(objConfigurationEntry.getGpID());
        
        if (l_objPieceSlot != null)
        {
            objConfigurationEntry.setPieceSlot(l_objPieceSlot);
            
            if (objConfigurationEntry.isMenu())
            {
                for (QCConfigurationEntry l_objConfigurationEntry : objConfigurationEntry.getListConfigurationEntry())
                    setPieceSlot(l_objConfigurationEntry, ar_HashPieceSlot);
            }
        }
    }

    public void add(Buildable b) {}

    private void RebuildToolBar() 
    {
        JToolBar l_objToolBar = m_Map.getToolBar();
        boolean l_bEndElementNotFound = true;
        int l_iStartPos = 0;
        
        if (m_objQCWorkingConfiguration != null)
        {
            // remove the old element
            for (int l_i = l_objToolBar.getComponents().length - 1; l_i >= 0; l_i--)
            {
                Component l_objComponent = l_objToolBar.getComponent(l_i);

                if (l_bEndElementNotFound)
                {
                    if (l_objComponent instanceof QCEndToolBarItem) 
                        l_bEndElementNotFound = false;                
                }
                else
                {
                    if (l_objComponent instanceof QCStartToolBarItem) 
                    {
                        l_iStartPos = l_i + 1;
                        break;
                    }
                    else
                    {
                        l_objToolBar.remove(l_i);               
                    }
                }
            }
            
            for (QCConfigurationEntry l_objConfigurationEntry : m_objQCWorkingConfiguration.getListConfigurationEntry())
            {
                if (l_objConfigurationEntry.getPieceSlot() != null)
                {
                    Component l_objComponent = CreateToolBarItem(l_objConfigurationEntry);
                    
                    if (l_objComponent != null)
                        l_objToolBar.add(l_objComponent, l_iStartPos++);                
                }
                
                l_objToolBar.validate();
            }
        }
    }

    private Component CreateToolBarItem(QCConfigurationEntry objConfigurationEntry) 
    {
        if (objConfigurationEntry.isMenu())
        { // submenu
            QCButtonMenu l_objQCButtonMenu = new QCButtonMenu(objConfigurationEntry.getPieceSlot());

            try 
            {
                //GetIconMenu l_objQCButton.setIcon(new ImageIcon(Op.load(l_strImageName + ".png").getImage(null)));
                l_objQCButtonMenu.setMargin(new Insets(0, 0, 0, 0));
                
                CreatePopupMenu(objConfigurationEntry, l_objQCButtonMenu);                
            } 
            catch (Exception ex) 
            {
                ex.printStackTrace();
            }

            l_objQCButtonMenu.setAlignmentY(0.0F);

            return l_objQCButtonMenu;
        }
        else // button standard
        {
            QCButton l_objQCButton = new QCButton(objConfigurationEntry.getPieceSlot());

            try 
            {
                l_objQCButton.InitDragDrop();
                //GetIcon l_objQCButton.setIcon(new ImageIcon(Op.load(l_strImageName + ".png").getImage(null)));
                l_objQCButton.setMargin(new Insets(0, 0, 0, 0));
            } 
            catch (Exception ex) 
            {
                ex.printStackTrace();
            }

            l_objQCButton.setAlignmentY(0.0F);
            
            return l_objQCButton;
        } 
    }

    private void CreatePopupMenu(QCConfigurationEntry objConfigurationEntry, QCButtonMenu objQCButtonMenu) 
    {
        JPopupMenu l_objPopupMenu = objQCButtonMenu.getPopupMenu();
        
        for (QCConfigurationEntry l_objConfigurationEntry : objConfigurationEntry.getListConfigurationEntry())
        {
            JMenuItem l_objMenuItem = CreateMenuItem(l_objConfigurationEntry);
            
            if (l_objMenuItem != null)
                l_objPopupMenu.add(l_objMenuItem);
        }
    }
    
    private JMenuItem CreateMenuItem(QCConfigurationEntry objConfigurationEntry) 
    {
        if (objConfigurationEntry.getPieceSlot() != null)
        {
            if (objConfigurationEntry.isMenu()) //submenu
            {
                JMenu l_objMenu = new JMenu();
                
                try 
                {
                    l_objMenu.setText(objConfigurationEntry.getPieceSlot().getPiece().getName()); // TODO
                    // GetIcon l_MenuItem.setIcon(new ImageIcon(Op.load(l_strImageName + l_strID + ".png").getImage(null)));
                } 
                catch (Exception ex) 
                {
                    ex.printStackTrace();
                }
                
                for (QCConfigurationEntry l_objConfigurationEntry : objConfigurationEntry.getListConfigurationEntry())
                {
                    JMenuItem l_objMenuItem = CreateMenuItem(l_objConfigurationEntry);

                    if (l_objMenuItem != null)
                        l_objMenu.add(l_objMenuItem);
                }
                
                return l_objMenu;
            }
            else
            {
                QCMenuItem l_MenuItem = new QCMenuItem(objConfigurationEntry.getPieceSlot());

                try 
                {
                    l_MenuItem.setText(objConfigurationEntry.getPieceSlot().getPiece().getName()); // TODO
                    // GetIcon l_MenuItem.setIcon(new ImageIcon(Op.load(l_strImageName + l_strID + ".png").getImage(null)));
                    l_MenuItem.InitDragDrop();
                } 
                catch (Exception ex) 
                {
                    ex.printStackTrace();
                }

                return l_MenuItem;
            }            
        }
        
        return null;
    }
    
    private void RebuildPopupMenu() 
    {
        JPopupMenu l_objPopupMenu = ((ASLMap)m_Map).getPopupMenu();
        int l_iStartPos = 0;
        boolean l_bEndElementNotFound = true;
        
        // remove the old element
        for (int l_i = l_objPopupMenu.getComponents().length - 1; l_i >= 0; l_i--)
        {
            Component l_objComponent = l_objPopupMenu.getComponent(l_i);
            
            if (l_bEndElementNotFound)
            {
                if (l_objComponent instanceof QCEndMenuItem) 
                    l_bEndElementNotFound = false;                
            }
            else
            {
                if (l_objComponent instanceof QCStartMenuItem) 
                {
                    l_iStartPos = l_i + 1;
                    break;
                }
                else
                {
                    l_objPopupMenu.remove(l_i);               
                }
            }
        }
        
        // title
        JMenuItem l_SelectQCItem = new JMenuItem("Select QC configuration");
        l_SelectQCItem.setBackground(new Color(255,255,255));
        l_objPopupMenu.add(l_SelectQCItem, l_iStartPos++);
        
        l_objPopupMenu.add(new JPopupMenu.Separator(), l_iStartPos++);

        // button group
        ButtonGroup l_Group = new ButtonGroup();
        
        for (QCConfiguration l_objQCConfiguration : mar_objListQCConfigurations)
        {
            QCRadioButtonMenuItem l_objQCRadioButtonMenuItem = new QCRadioButtonMenuItem(l_objQCConfiguration);

            l_objQCRadioButtonMenuItem.addActionListener(new ActionListener() 
            {
                public void actionPerformed(ActionEvent evt) 
                {
                    if (evt.getSource() instanceof QCRadioButtonMenuItem)
                    {
                        m_objQCWorkingConfiguration = ((QCRadioButtonMenuItem)evt.getSource()).getQCConfiguration();
                        saveWorkingConfiguration();
                        RebuildToolBar();
                    }
                }
            });

            l_Group.add(l_objQCRadioButtonMenuItem);
            l_objPopupMenu.add(l_objQCRadioButtonMenuItem, l_iStartPos++);
            
            l_objQCRadioButtonMenuItem.setSelected(l_objQCConfiguration == m_objQCWorkingConfiguration);
        }
    }
}
