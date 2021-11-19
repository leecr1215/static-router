
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jnetpcap.PcapIf;

public class ARPDlg extends JFrame implements BaseLayer {

   public int nUpperLayerCount = 0;
   public String pLayerName = null;
   public BaseLayer p_UnderLayer = null;
   public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

   public static LayerManager m_LayerMgr = new LayerManager();

   private JTextField ChattingWrite;
   private JTextField PathWrite;
   private JTextField dstIpWrite;
   private JTextField proxyDeviceWrite;
   private JTextField proxyIpWrite;
   private JTextField proxyMacWrite;
   private JTextField routeDestinationWrite;
   private JTextField routeNetMaskWrite;
   private JTextField routeGatewayWrite;
   private JTextField routeInterfaceWrite;
   
   Container contentPane;

   JTextArea routerTableArea;
   JTextArea srcMacAddress;
   JTextArea srcIpAddress;
   JTextArea cacheArea;
   JTextArea proxyArpArea;

   JLabel lblsrc;
   JLabel lbldst;
   JLabel dstIpLabel;
   JLabel proxyDevice;
   JLabel proxyIp;
   JLabel proxyMac;
   JLabel routeDestination;
   JLabel routeNetMask;
   JLabel routeGateway;
   JLabel routeInterface;
   

   JButton Setting_Button;
   JButton Chat_send_Button;
   JButton File_send_Button;
   JButton openFileButton;
   JButton itemDeleteButton;
   JButton allDeleteButton;
   JButton dstIpSendButton;
   JButton proxyAddButton;
   JButton proxyDeleteButton;
   JButton routeAddButton;

   static JComboBox<String> NICComboBox;

   int adapterNumber = 0;
   byte[] srcIPNumber, dstIPNumber, srcMacNumber;
   String Text;
   JProgressBar progressBar;

   File file;
   
   private ArrayList<ArrayList<byte[]>> cacheTable = new ArrayList<ArrayList<byte[]>>();
   
   public static void main(String[] args) {

      ////////////////

      m_LayerMgr.AddLayer(new NILayer("NI"));
      m_LayerMgr.AddLayer(new EthernetLayer("Ethernet"));
      m_LayerMgr.AddLayer(new ARPLayer("ARP"));
      m_LayerMgr.AddLayer(new IPLayer("IP"));
      m_LayerMgr.AddLayer(new TCPLayer("TCP"));
      // m_LayerMgr.AddLayer(new ChatAppLayer("ChatApp"));
      // m_LayerMgr.AddLayer(new FileAppLayer("FileApp"));
      m_LayerMgr.AddLayer(new ARPDlg("GUI"));
      m_LayerMgr.ConnectLayers(" NI ( *Ethernet ( *ARP ( *IP ( *TCP ( *GUI ) ) ) ) )");
      ///////////////////
   }

   class setAddressListener implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent e) {

         if (e.getSource() == Setting_Button) {

            if (Setting_Button.getText() == "Reset") {
               srcMacAddress.setText("");
               srcIpAddress.setText("");
               Setting_Button.setText("Setting");
               srcMacAddress.setEnabled(true);
               srcIpAddress.setEnabled(true);
            } else {
               byte[] MacAddress = new byte[6];
               byte[] IpAddress = new byte[4];

               String srcMac = srcMacAddress.getText();
               String srcIP = srcIpAddress.getText();
               System.out.println("srcMacAddress : " + srcMac);
               System.out.println("srcIPAddress : " + srcIP);

               String[] byte_srcMac = srcMac.split("-");
               for (int i = 0; i < 6; i++) {
                  MacAddress[i] = (byte) Integer.parseInt(byte_srcMac[i], 16);
               }

               String[] byte_srcIp = srcIP.split("\\.");
               for (int i = 0; i < 4; i++) {
                  IpAddress[i] = (byte) Integer.parseInt(byte_srcIp[i]);
               }

               srcIPNumber = IpAddress;
               srcMacNumber = MacAddress;
               
               String[] dstMac = {"ff","ff","ff","ff","ff","ff"};
               byte[] dstMacAddress = new byte[6];
               
               for (int i = 0; i < 6; i++) {
                  dstMacAddress[i] = (byte) Integer.parseInt(dstMac[i], 16);
               }
               
               ((EthernetLayer)m_LayerMgr.GetLayer("Ethernet")).SetEnetSrcAddress(MacAddress);
               ((EthernetLayer)m_LayerMgr.GetLayer("Ethernet")).SetEnetDstAddress(dstMacAddress);
               
               ((ARPLayer)m_LayerMgr.GetLayer("ARP")).SetArpSrcAddress(MacAddress);
               ((ARPLayer)m_LayerMgr.GetLayer("ARP")).SetArpDstAddress(dstMacAddress);
               ((ARPLayer)m_LayerMgr.GetLayer("ARP")).SetIpSrcAddress(IpAddress);
               System.out.println("ARPDlg에서 IPAddress는? " + Byte.toUnsignedInt(IpAddress[2]) +"."+Byte.toUnsignedInt(IpAddress[3]));
               
               ((NILayer) m_LayerMgr.GetLayer("NI")).SetAdapterNumber(adapterNumber);

               Setting_Button.setText("Reset");
               srcMacAddress.setEnabled(false);
               srcIpAddress.setEnabled(false);

            }
         }
         // basic ARP 전송
         if (e.getSource() == dstIpSendButton) {
            if (dstIpSendButton.getText() == "Send") {
               String dstIP = dstIpWrite.getText();
               cacheArea.append(dstIP);
               cacheArea.append("  ??-??-??-??-??-??");
               cacheArea.append("  Incomplete" + "\n");
               byte[] dstIPAddress = new byte[4];
               String[] byte_dstIP = dstIP.split("\\.");
               for (int i = 0; i < 4; i++) {
                  dstIPAddress[i] = (byte) Integer.parseInt(byte_dstIP[i], 10);
               }
               dstIPNumber = dstIPAddress;
               ((TCPLayer) m_LayerMgr.GetLayer("TCP")).ARPSend(srcIPNumber, dstIPNumber);
               
            }
         }
         // proxy ARP 전송
         if (e.getSource() == proxyAddButton) {
            //proxy Add 
            if (proxyAddButton.getText() == "Add") {
               String proxyDevice = proxyDeviceWrite.getText();
               String proxyIP = proxyIpWrite.getText();
               String proxyMac = proxyMacWrite.getText();
               proxyArpArea.append("Interface0");
               proxyArpArea.append("  " + proxyIP);
               proxyArpArea.append("  " + proxyMac + "\n");
               
               byte[] proxyInterfaceByte = new byte[1];
               byte[] proxyIpByte = new byte[4];
               byte[] proxyMacByte = new byte[6];
               String[] ip_split = proxyIP.split("\\.");
               for (int i = 0; i < 4; i++) {
                  proxyIpByte[i] = (byte) Integer.parseInt(ip_split[i], 10);
               }
               
               String[] mac_split = proxyMac.split("-");
               for (int i = 0; i < 6; i++) {
                  proxyMacByte[i] = (byte) Integer.parseInt(mac_split[i], 16);
               }
               
               proxyInterfaceByte[0] = (byte)Integer.parseInt("1");
               ((ARPLayer)m_LayerMgr.GetLayer("ARP")).addProxyTable(proxyInterfaceByte, proxyIpByte, proxyMacByte);
            }
            //proxy Delete 
            else if(proxyAddButton.getText() == "Delete") {
               //Delete 구현 
            }
         }
         
         //Chatting send 
         if (e.getSource() == Chat_send_Button) {
            if (Setting_Button.getText() == "Reset") {
               for (int i = 0; i < 10; i++) {
                  String input = ChattingWrite.getText();
                  routerTableArea.append("[SEND] : " + input + "\n");
                  byte[] bytes = input.getBytes();
                  m_LayerMgr.GetLayer("ChatApp").Send(bytes, bytes.length);
                  if (m_LayerMgr.GetLayer("GUI").Receive()) {
                     input = Text;
                     routerTableArea.append("[RECV] : " + input + "\n");
                     continue;
                  }
                  break;
               }
            } else {
               JOptionPane.showMessageDialog(null, "Address Configuration Error");
            }
         }
        
       
      }

   }

   public ARPDlg(String pName) {
      pLayerName = pName;

      setTitle("Packet_Send_Test");
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setBounds(250, 250, 750, 700);
      contentPane = new JPanel();
      ((JComponent) contentPane).setBorder(new EmptyBorder(5, 5, 5, 5));
      setContentPane(contentPane);
      contentPane.setLayout(null);

      // ARP Cache panel
      JPanel arpCachePanel = new JPanel();
      arpCachePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "ARP Cache",
            TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
      arpCachePanel.setBounds(10, 5, 370, 371);
      contentPane.add(arpCachePanel);
      arpCachePanel.setLayout(null);

      JPanel arpCacheEditorPanel = new JPanel();
      arpCacheEditorPanel.setBounds(10, 15, 350, 230);
      arpCachePanel.add(arpCacheEditorPanel);
      arpCacheEditorPanel.setLayout(null);

      cacheArea = new JTextArea();
      cacheArea.setEditable(false);
      cacheArea.setBounds(0, 0, 350, 220);
      arpCacheEditorPanel.add(cacheArea);// chatting edit

      itemDeleteButton = new JButton("Item Delete");
      itemDeleteButton.setBounds(70, 250, 100, 30);

      allDeleteButton = new JButton("All Delete");
      allDeleteButton.setBounds(200, 250, 100, 30);
      /* add Action Listener for delete button */
      arpCachePanel.add(itemDeleteButton);
      arpCachePanel.add(allDeleteButton);

      dstIpLabel = new JLabel("IP_Addr");
      dstIpLabel.setBounds(15, 300, 100, 20);
      arpCachePanel.add(dstIpLabel);

      dstIpWrite = new JTextField();
      dstIpWrite.setBounds(70, 300, 200, 20);// 249
      arpCachePanel.add(dstIpWrite);
      dstIpWrite.setColumns(10);// target ip address writing area
      dstIpSendButton = new JButton("Send");
      dstIpSendButton.addActionListener(new setAddressListener());
      dstIpSendButton.setBounds(285, 300, 70, 20);
      arpCachePanel.add(dstIpSendButton);

      // proxy arp entry panel
      JPanel proxyArpPanel = new JPanel();
      proxyArpPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Proxy Arp Entry",
            TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
      proxyArpPanel.setBounds(380, 5, 350, 370);
      contentPane.add(proxyArpPanel);
      proxyArpPanel.setLayout(null);

      JPanel proxyEditorPanel = new JPanel();// proxy editor panel
      proxyEditorPanel.setBounds(5, 15, 330, 160);
      proxyArpPanel.add(proxyEditorPanel);
      proxyEditorPanel.setLayout(null);

      proxyArpArea = new JTextArea();
      proxyArpArea.setEditable(false);
      proxyArpArea.setBounds(5, 5, 420, 150);
      proxyEditorPanel.add(proxyArpArea);// proxy arp entry

      JPanel proxyInputPanel = new JPanel();
      proxyInputPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
      proxyInputPanel.setBounds(10, 200, 320, 150);
      proxyInputPanel.setLayout(null);
      proxyArpPanel.add(proxyInputPanel);

      proxyDevice = new JLabel("Device");
      proxyDevice.setBounds(20, 10, 60, 20);
      proxyInputPanel.add(proxyDevice);

      proxyIp = new JLabel("IP 주소");
      proxyIp.setBounds(20, 40, 60, 20);
      proxyInputPanel.add(proxyIp);

      proxyMac = new JLabel("Mac 주소");
      proxyMac.setBounds(20, 70, 60, 20);
      proxyInputPanel.add(proxyMac);

      proxyDeviceWrite = new JTextField();
      proxyDeviceWrite.setBounds(100, 10, 200, 20);
      proxyInputPanel.add(proxyDeviceWrite);

      proxyIpWrite = new JTextField();
      proxyIpWrite.setBounds(100, 40, 200, 20);
      proxyInputPanel.add(proxyIpWrite);

      proxyMacWrite = new JTextField();
      proxyMacWrite.setBounds(100, 70, 200, 20);
      proxyInputPanel.add(proxyMacWrite);

      proxyAddButton = new JButton("Add");
      proxyAddButton.setBounds(70, 100, 80, 30);
      proxyDeleteButton = new JButton("Delete");
      proxyDeleteButton.setBounds(180, 100, 80, 30);
      proxyInputPanel.add(proxyAddButton);
      proxyInputPanel.add(proxyDeleteButton);
      
      proxyAddButton.addActionListener(new setAddressListener());
      proxyDeleteButton.addActionListener(new setAddressListener());
      
      // routing table pannel
      JPanel routePannel = new JPanel();
      routePannel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Static Routing Table",
            TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
      routePannel.setBounds(10, 380, 360, 260);
      contentPane.add(routePannel);
      routePannel.setLayout(null);

      JPanel routerTableEditorPanel = new JPanel();// chatting write panel
      routerTableEditorPanel.setBounds(10, 15, 340, 235);
      routePannel.add(routerTableEditorPanel);
      routerTableEditorPanel.setLayout(null);

      routerTableArea = new JTextArea();
      routerTableArea.setEditable(false);
      routerTableArea.setBounds(0, 0, 340, 240);
      routerTableEditorPanel.add(routerTableArea);// chatting edit


      routerTableArea.setLayout(null);
      

      // router add panel
      JPanel routerAddPanel = new JPanel();// router add panel
      routerAddPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Add Routing Table",
            TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
      routerAddPanel.setBounds(380, 380, 350, 250);
      
      routerAddPanel.setLayout(null);

      routeDestination = new JLabel("Destination");
      routeDestination.setBounds(20, 40, 80, 20);
      routerAddPanel.add(routeDestination);

      routeNetMask = new JLabel("Netmask");
      routeNetMask.setBounds(20, 70, 80, 20);
      routerAddPanel.add(routeNetMask);
      
      routeGateway = new JLabel("Gateway");
      routeGateway.setBounds(20, 100, 80, 20);
      routerAddPanel.add(routeGateway);
      
      routeGateway = new JLabel("Flag");
      routeGateway.setBounds(20, 130, 80, 20);
      routerAddPanel.add(routeGateway);
      
      routeInterface = new JLabel("Interface");
      routeInterface.setBounds(20, 160, 80, 20);
      routerAddPanel.add(routeInterface);
      
      routeDestinationWrite = new JTextField();
      routeDestinationWrite.setBounds(100, 40, 200, 20);
      routerAddPanel.add(routeDestinationWrite);

      routeNetMaskWrite = new JTextField();
      routeNetMaskWrite.setBounds(100, 70, 200, 20);
      routerAddPanel.add(routeNetMaskWrite);

      routeGatewayWrite = new JTextField();
      routeGatewayWrite.setBounds(100, 100, 200, 20);
      routerAddPanel.add(routeGatewayWrite);
      
      routeInterfaceWrite = new JTextField();
      routeInterfaceWrite.setBounds(100, 160, 200, 20);
      routerAddPanel.add(routeInterfaceWrite);
      
      routeAddButton = new JButton("Add");
      routeAddButton.setBounds(130, 200, 70, 30);
      
      JCheckBox up = new JCheckBox("up");
      up.setBounds(100, 130, 50, 20);
	  JCheckBox gateway = new JCheckBox("gateway", true);
	  gateway.setBounds(150, 130, 80, 20);
	  JCheckBox host = new JCheckBox("host");
	  host.setBounds(230, 130, 100, 20);
		
	  routerAddPanel.add(up);
	  routerAddPanel.add(gateway);
	  routerAddPanel.add(host);
	  routerAddPanel.add(routeAddButton);
	  
	  contentPane.add(routerAddPanel);
      setVisible(true);

   }

   public File getFile() {
      return this.file;
   }

   public String get_MacAddress(byte[] byte_MacAddress) {

      String MacAddress = "";
      for (int i = 0; i < 6; i++) {
         MacAddress += String.format("%02X%s", byte_MacAddress[i], (i < MacAddress.length() - 1) ? "" : "");
         if (i != 5) {
            MacAddress += "-";
         }
      }

      System.out.println("present MAC address: " + MacAddress);
      return MacAddress;
   }

   public boolean Receive(byte[] input) {
      if (input != null) {
         byte[] data = input;
         Text = new String(data);
         routerTableArea.append("[RECV] : " + Text + "\n");
         return false;
      }
      return false;
   }

   @Override
   public void SetUnderLayer(BaseLayer pUnderLayer) {
      // TODO Auto-generated method stub
      if (pUnderLayer == null)
         return;
      this.p_UnderLayer = pUnderLayer;
   }

   @Override
   public void SetUpperLayer(BaseLayer pUpperLayer) {
      // TODO Auto-generated method stub
      if (pUpperLayer == null)
         return;
      this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
      // nUpperLayerCount++;
   }

   @Override
   public String GetLayerName() {
      // TODO Auto-generated method stub
      return pLayerName;
   }

   @Override
   public BaseLayer GetUnderLayer() {
      // TODO Auto-generated method stub
      if (p_UnderLayer == null)
         return null;
      return p_UnderLayer;
   }

   @Override
   public BaseLayer GetUpperLayer(int nindex) {
      // TODO Auto-generated method stub
      if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
         return null;
      return p_aUpperLayer.get(nindex);
   }

   @Override
   public void SetUpperUnderLayer(BaseLayer pUULayer) {
      this.SetUpperLayer(pUULayer);
      pUULayer.SetUnderLayer(this);

   }

   // cache table setting
   // ip , ethernet , status(0,1)
   public void setArpCache(ArrayList<ArrayList<byte[]>> cacheTable) {
      this.cacheTable = cacheTable;
      cacheArea.setText("");
      //byte[] ipAddressByte = new byte[4];
      //byte[] macAddressByte = new byte[6];
      System.out.println("set arp cache");

      for(int i=0; i<cacheTable.size(); i++) {
         byte[] ip_byte = cacheTable.get(i).get(0);
         byte[] mac_byte = cacheTable.get(i).get(1);
         byte[] status_byte = cacheTable.get(i).get(2);
         
         String ipByte1 = Integer.toString(Byte.toUnsignedInt(ip_byte[0]));
         String ipByte2 = Integer.toString(Byte.toUnsignedInt(ip_byte[1]));
         String ipByte3 = Integer.toString(Byte.toUnsignedInt(ip_byte[2]));
         String ipByte4 = Integer.toString(Byte.toUnsignedInt(ip_byte[3]));
         
         String macByte1 = Integer.toHexString(Byte.toUnsignedInt(mac_byte[0]));
         String macByte2 = Integer.toHexString(Byte.toUnsignedInt(mac_byte[1]));
         String macByte3 = Integer.toHexString(Byte.toUnsignedInt(mac_byte[2]));
         String macByte4 = Integer.toHexString(Byte.toUnsignedInt(mac_byte[3]));
         String macByte5 = Integer.toHexString(Byte.toUnsignedInt(mac_byte[4]));
         String macByte6 = Integer.toHexString(Byte.toUnsignedInt(mac_byte[5]));
         
         cacheArea.append(ipByte1+"."+ipByte2+"."+ipByte3+"."+ipByte4);
         cacheArea.append("  "+macByte1+"-"+macByte2+"-"+macByte3+"-"+macByte4+"-"+macByte5+"-"+macByte6);
         System.out.println(ipByte1+"."+ipByte2+"."+ipByte3+"."+ipByte4);
         System.out.println("  "+macByte1+"-"+macByte2+"-"+macByte3+"-"+macByte4+"-"+macByte5+"-"+macByte6);

         if (byte2ToInt(status_byte[0], status_byte[1])==1) {
            cacheArea.append("  complete" + "\n");
         }
         else {
            cacheArea.append("  Incomplete" + "\n");
         }
         
      }
      
   }
   
   private int byte2ToInt(byte value1, byte value2) {
        return (int)((value1 << 8) | (value2));
    }
}