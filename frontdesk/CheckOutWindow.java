package frontdesk;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSigProperties;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSignDesigner;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

import integrisign.IDocInfo;
import integrisign.IGrabber;
import integrisign.desktop.Base64Format;
import integrisign.desktop.DeskSign;
import integrisign.desktop.ValidationException;

/**
 * This class is to CheckOut a work order.
 * @author elias
 *
 */
public class CheckOutWindow extends WorkOrder implements ActionListener, IDocInfo{
	private WorkOrder wo;
	private LinkedList<WorkOrder> listOfCompletedWorkOrder = new LinkedList<WorkOrder>();
	private String nameOfSigner = "";
	private DeskSign sign;
 
    
	public CheckOutWindow() {
	}
	
	void setWO(WorkOrder wo) {
		this.wo = wo;
	}
	
	private void insertSignatureField(Path filePath){
		File file = new File(filePath.toUri());
		PDDocument doc;
		try {
			doc = PDDocument.load(file);
			PDResources resources = new PDResources();
			PDAcroForm acroForm = new PDAcroForm(doc);
			doc.getDocumentCatalog().setAcroForm(acroForm);
			acroForm.setDefaultResources(resources);
			PDSignatureField signatureField = new PDSignatureField(acroForm);
			PDAnnotationWidget signatureWidget = signatureField.getWidgets().get(0);
            PDRectangle signatureRect = new PDRectangle(120, 10, 150, 30);
            signatureWidget.setRectangle(signatureRect);
            PDPage page = doc.getPage(0);
            signatureWidget.setPage(page);
            page.getAnnotations().add(signatureWidget);
            acroForm.getFields().add(signatureField);
            doc.save(file);
            doc.close();
		} catch (Exception e) {
		}
	}
	
	void setSignerInfo(String name) {
		this.nameOfSigner = name;
	}
	
	private void updateSignerInfo() {
		wo.nameOfSigner = this.nameOfSigner;
	}
	
	void sign(Path filePath, Path destPath) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException, ValidationException {
		File ksFile = new File("CEB_FRONTDESK.p12");
		DeskSign sign = new DeskSign();
		sign.signNowEx(nameOfSigner, nameOfSigner, false, (IDocInfo)this);
		Base64Format b64fmt = new Base64Format();
		byte[] imageBytes = b64fmt.decode64(sign.getPNGString(sign.getString(), 125, 85, true));
		InputStream imageStream = new ByteArrayInputStream(imageBytes);
		DigitalSign signing;
		File file = new File(filePath.toUri());
		File destFile = new File(destPath.toUri());
		boolean isFileExist=false;
		if(FrontDesk.overwriteOption==0 || !isFileExist) {
			KeyStore keystore = KeyStore.getInstance("PKCS12");
			char[] pin = "C3bFr0ntD3$k".toCharArray();
			keystore.load(new FileInputStream(ksFile), pin);
			signing = new DigitalSign(keystore, pin.clone());
			int page = 1;
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yy");
			PDDocument doc;
			
			doc = PDDocument.load(file);
			int sizeX = 0;
	        PDPage pdPage = doc.getPage(0);
	        PDAcroForm form = new PDAcroForm(doc);
	        doc.getDocumentCatalog().setAcroForm(form);
	        PDFont font = PDType1Font.TIMES_ROMAN;
	        PDResources resources = new PDResources();
	      
	        resources.put(COSName.getPDFName("Time"), font);
	        form.setDefaultResources(resources);
	        PDTextField dateField = new PDTextField(form);
	        PDTextField nameField = new PDTextField(form);
	        dateField.setPartialName("DateField");
	        nameField.setPartialName("NameField");
	        String defaultAppearance = "/Time 12 Tf 0 g";
	        dateField.setDefaultAppearance(defaultAppearance);
	        nameField.setDefaultAppearance(defaultAppearance);
	        
	        
			
	        form.getFields().add(dateField);
	        form.getFields().add(nameField);
	        PDAnnotationWidget dateWidget = dateField.getWidgets().get(0);
	        PDAnnotationWidget nameWidget = nameField.getWidgets().get(0);
	        float width = font.getStringWidth(nameOfSigner) / 1000;
	        sizeX = (Math.round(width * font.getWidthFromFont(12))/20);
	        PDRectangle dateRect = new PDRectangle(462, 9, 150, 30);
	        PDRectangle nameRect = new PDRectangle(115, 9, sizeX, 30);
		    dateWidget.setRectangle(dateRect);
		    nameWidget.setRectangle(nameRect);
		    dateWidget.setPage(pdPage);    
		    nameWidget.setPage(pdPage);
		    dateWidget.setPrinted(true);
		    nameWidget.setPrinted(true);
		    pdPage.getAnnotations().add(dateWidget);
		    pdPage.getAnnotations().add(nameWidget);
		    dateField.setReadOnly(true);
		    nameField.setReadOnly(true);
		    dateField.setValue(sdf.format(date));
		    nameField.setValue(nameOfSigner);
		    doc.save(file);
		    doc.close();
		  
			int signX = sizeX + 115;
			signing.setVisibleSignDesigner(file.toString(), signX, 755, -75, imageStream, page);
			signing.setVisibleSignatureProperties(nameOfSigner, "", "PickUp", 0, 1, true);
			signing.setExternalSigning(false);
			if(FrontDesk.overwriteOption==0) {
				File tempFile = new File(destPath.toString()+".tmp");
				signing.signPDF(file, tempFile, null);
				Files.move(tempFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} else {
			signing.signPDF(file, destFile, null);
			}
		}
		

		updateSignerInfo();
	}
	
	private boolean fileExistChecker(Path filePath) {
		File file = new File(filePath.toUri());
		return file.exists(); 
	}
	
	void signByDepartment(HashMap<WorkOrder, LinkedList<Path>> workOrderPathMap) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, ValidationException {
		File ksFile = new File("CEB_FRONTDESK.p12");
		DeskSign sign = new DeskSign();
		sign.signNowEx(nameOfSigner, nameOfSigner, false, (IDocInfo)this);
		Base64Format b64fmt = new Base64Format();
		byte[] imageBytes = b64fmt.decode64(sign.getPNGString(sign.getString(), 125, 85, true));
		
		DigitalSign signing;
		KeyStore keystore = KeyStore.getInstance("PKCS12");
		char[] pin = "C3bFr0ntD3$k".toCharArray();
		keystore.load(new FileInputStream(ksFile), pin);
		signing = new DigitalSign(keystore, pin.clone());
		int page = 1;
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yy");
		File file;
		File destFile;
		
		for(WorkOrder woKey : workOrderPathMap.keySet()) {
			int overwriteOption = -1;
			boolean overwrite = false;
			boolean isFileExist = fileExistChecker(workOrderPathMap.get(woKey).get(1));
			file = new File(workOrderPathMap.get(woKey).get(0).toUri());
			Path destPath = workOrderPathMap.get(woKey).get(1);
			destFile = new File(destPath.toUri());
			 
			if(isFileExist) {
				overwriteOption = JOptionPane.showConfirmDialog(new JFrame(), "WorkOrder : " + woKey.numberOfWorkOrder + "\nFile is already exist in archive.\nTo overwrite, Click OK.\nOtherwise, Cancel.", "Overwrite", JOptionPane.OK_CANCEL_OPTION);
				if(overwriteOption!=0) {
					overwrite = false;
					continue;
				} else {
					overwrite = true;
				}
			} 
			
			if((!isFileExist) || overwrite) {
				InputStream imageStream = new ByteArrayInputStream(imageBytes);
				PDDocument doc = PDDocument.load(file);
				Date date = new Date();
				int sizeX = 0;
				PDPage pdPage = doc.getPage(0);
				PDAcroForm form = new PDAcroForm(doc);
				doc.getDocumentCatalog().setAcroForm(form);
				PDFont font = PDType1Font.TIMES_ROMAN;
				PDResources resources = new PDResources();
				resources.put(COSName.getPDFName("Time"), font);
				form.setDefaultResources(resources);
				PDTextField dateField = new PDTextField(form);
				PDTextField nameField = new PDTextField(form);
				dateField.setPartialName("DateField");
				nameField.setPartialName("NameField");
				String defaultAppearance = "/Time 12 Tf 0 g";
				dateField.setDefaultAppearance(defaultAppearance);
				nameField.setDefaultAppearance(defaultAppearance);
				form.getFields().add(dateField);
				form.getFields().add(nameField);
				PDAnnotationWidget dateWidget = dateField.getWidgets().get(0);
				PDAnnotationWidget nameWidget = nameField.getWidgets().get(0);
				float width = font.getStringWidth(nameOfSigner) / 1000;
				sizeX = (Math.round(width * font.getWidthFromFont(12))/20);
				PDRectangle dateRect = new PDRectangle(462, 9, 150, 30);
				PDRectangle nameRect = new PDRectangle(115, 9, sizeX, 30);
				dateWidget.setRectangle(dateRect);
				nameWidget.setRectangle(nameRect);
				dateWidget.setPage(pdPage);    
				nameWidget.setPage(pdPage);
				dateWidget.setPrinted(true);
				nameWidget.setPrinted(true);
				pdPage.getAnnotations().add(dateWidget);
				pdPage.getAnnotations().add(nameWidget);
				dateField.setReadOnly(true);
				nameField.setReadOnly(true);
				dateField.setValue(sdf.format(date));
				nameField.setValue(nameOfSigner);
				doc.save(file);
				doc.close();
				int signX = sizeX + 115;
				signing.setVisibleSignDesigner(file.toString(), signX, 755, -75, imageStream, page);
				signing.setVisibleSignatureProperties(nameOfSigner, "", "PickUp", 0, 1, true);
				signing.setExternalSigning(false);
				if(overwrite) {
					File tempFile = new File(destPath.toString()+".tmp");
					signing.signPDF(file, tempFile,null);
					Files.move(tempFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
					listOfCompletedWorkOrder.add(woKey);
					woKey.setSigner(nameOfSigner);
				} else {
					signing.signPDF(file, destFile, null);
					listOfCompletedWorkOrder.add(woKey);
					woKey.setSigner(nameOfSigner);
				}
			}
		}
	}
	
	LinkedList<WorkOrder> getCompletedWorkOrderByDepartment(){
		return listOfCompletedWorkOrder;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
	}

	@Override
	public void feedGrabber(IGrabber arg0) {
	}

	@Override
	public String getDocID() {
		return null;
	}

	@Override
	public byte getVersion() {
		return 0;
	}
}

class DigitalSign extends CreateSignatureBase {
	private SignatureOptions signatureOptions;
	private PDVisibleSignDesigner visibleSignDesigner;
	private final PDVisibleSigProperties visibleSignatureProperties = new PDVisibleSigProperties();
	private boolean lateExternalSigning = false;
	
	public DigitalSign(KeyStore keystore, char[] pin) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, CertificateException {
		super(keystore, pin);
	}
	
	public void signPDF(File inputFile, File signedFile, String tsaUrl) throws IOException {
		this.signPDF(inputFile, signedFile, tsaUrl, null);
	}
	
	private void signPDF(File inputFile, File signedFile, String tsaUrl, String signatureFieldName) throws IOException {
		if (inputFile == null || !inputFile.exists())
		{
			throw new IOException("Document for signing does not exist");
		}
		
		setTsaUrl(tsaUrl);
		try (FileOutputStream fos = new FileOutputStream(signedFile);
				PDDocument doc = PDDocument.load(inputFile))
		{
			int accessPermissions = SigUtils.getMDPPermission(doc);
			if (accessPermissions == 1) {
				throw new IllegalStateException("No changes to the document are permitted due to DocMDP transform parameters dictionary");
			}
			PDSignature signature;
			signature = findExistingSignature(doc, signatureFieldName);
			if (signature == null){
				signature = new PDSignature();
			}
			
			if (doc.getVersion() >= 1.5f && accessPermissions == 0) {
				SigUtils.setMDPPermission(doc, signature, 2);
			}
			
			PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
			if (acroForm != null && acroForm.getNeedAppearances()) {
				if (acroForm.getFields().isEmpty()) {
					acroForm.getCOSObject().removeItem(COSName.NEED_APPEARANCES);
				} else {
					System.out.println("/NeedAppearances is set, signature may be ignored by Adobe Reader");
				}
			}
			signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
			signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);

			if (visibleSignatureProperties != null) {
				visibleSignatureProperties.buildSignature();
				signature.setName(visibleSignatureProperties.getSignerName());
				signature.setLocation(visibleSignatureProperties.getSignerLocation());
				signature.setReason(visibleSignatureProperties.getSignatureReason());
			}
			signature.setSignDate(Calendar.getInstance());
			SignatureInterface signatureInterface = isExternalSigning() ? null : this;
			if (visibleSignatureProperties != null && visibleSignatureProperties.isVisualSignEnabled()) {
				signatureOptions = new SignatureOptions();
				signatureOptions.setVisualSignature(visibleSignatureProperties.getVisibleSignature());
				signatureOptions.setPage(visibleSignatureProperties.getPage() - 1);
				doc.addSignature(signature, signatureInterface, signatureOptions);
			} else {
				doc.addSignature(signature, signatureInterface);
			}
			doc.saveIncremental(fos);
		}
		IOUtils.closeQuietly(signatureOptions);
	}

	private PDSignature findExistingSignature(PDDocument doc, String sigFieldName) {
		PDSignature signature = null;
		PDSignatureField signatureField;
		PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
		if (acroForm != null) {
			signatureField = (PDSignatureField) acroForm.getField(sigFieldName);
			if (signatureField != null)	{
				signature = signatureField.getSignature();
				if (signature == null) {
					signature = new PDSignature();
					signatureField.getCOSObject().setItem(COSName.V, signature);
				} else {
					throw new IllegalStateException("The signature field " + sigFieldName + " is already signed.");
				}
			}
		}
		return signature;
	}
	    
	void setVisibleSignDesigner(int zoomPercent, InputStream imageStream) throws IOException {
		visibleSignDesigner = new PDVisibleSignDesigner(imageStream);
		visibleSignDesigner.zoom(zoomPercent);
	}

	void setVisibleSignatureProperties(String name, String location, String reason, int preferredSize, int page, boolean visualSignEnabled) {
		visibleSignatureProperties.signerName(name).signerLocation(location).signatureReason(reason).
		preferredSize(preferredSize).page(page).visualSignEnabled(visualSignEnabled).
		setPdVisibleSignature(visibleSignDesigner);
	}

	void setVisibleSignDesigner(String filename, int x, int y, int zoomPercent, InputStream imageStream, int page) throws IOException {
		visibleSignDesigner = new PDVisibleSignDesigner(filename, imageStream, page);
		visibleSignDesigner.xAxis(x).yAxis(y).zoom(zoomPercent).adjustForRotation();
	}
}