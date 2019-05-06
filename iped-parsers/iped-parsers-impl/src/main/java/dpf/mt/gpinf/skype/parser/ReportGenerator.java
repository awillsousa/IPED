package dpf.mt.gpinf.skype.parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import org.apache.tika.metadata.Metadata;
import org.xml.sax.ContentHandler;

import dpf.sp.gpinf.indexer.parsers.util.Messages;
import dpf.sp.gpinf.indexer.parsers.util.Util;
import dpf.sp.gpinf.indexer.util.IOUtil;
import dpf.sp.gpinf.indexer.util.SimpleHTMLEncoder;
import iped3.io.ItemBase;
import iped3.search.ItemSearcher;


/**
 * Classe responsável por gerar a representação HTML dos registros encontrados
 * pelo SkypeParser
 *
 * @author Patrick Dalla Bernardina patrick.pdb@dpf.gov.br
 */
public class ReportGenerator {
	
	private static final String NEW_ROW = "<TR><TD>"; //$NON-NLS-1$
	private static final String CLOSE_ROW = "</TD></TR>"; //$NON-NLS-1$
	private static final String NEW_COL = "</TD><TD>"; //$NON-NLS-1$
	
	ContentHandler handler;
	Metadata metadata;
	String skypeName;
	
	public ReportGenerator(ContentHandler handler, Metadata metadata, String skypeName){
		this.handler = handler;
		this.metadata = metadata;
		this.skypeName = skypeName;
	}
	
	public void startDocument(PrintWriter out){
        out.println("<!DOCTYPE html>"); //$NON-NLS-1$
        out.println("<HTML>"); //$NON-NLS-1$
        out.println("<HEAD>"); //$NON-NLS-1$
        out.print("<meta charset=\"UTF-8\">"); //$NON-NLS-1$
        out.println("<style>"+ //$NON-NLS-1$
                "TABLE {  border-collapse: collapse; font-family: Arial, sans-serif; } "  //$NON-NLS-1$
                + "TH { border: solid; font-weight: bold; text-align: center; background-color:#AAAAAA; foreground-color:#FFFFFF; } " //$NON-NLS-1$
                + "TR { vertical-align: middle; } "  //$NON-NLS-1$
                + ".rb { background-color:#E7E7E7; vertical-align: middle; } " //$NON-NLS-1$
                + ".rr {  background-color:#FFFFFF; vertical-align: middle; } " //$NON-NLS-1$
                + "TD { border: solid; border-width: thin; padding: 3px; text-align: left; vertical-align: middle; word-wrap: break-word; } " //$NON-NLS-1$
                + ".e { display: table-cell; border: solid; border-width: thin; padding: 3px; text-align: center; vertical-align: middle; word-wrap: break-word; width: 150px; font-family: monospace; } " //$NON-NLS-1$
                + ".a { display: table-cell; border: solid; border-width: thin; padding: 3px; text-align: center; vertical-align: middle; word-wrap: break-word; width: 110px; } " //$NON-NLS-1$
                + ".b { display: table-cell; border: solid; border-width: thin; padding: 3px; text-align: left; vertical-align: middle; word-wrap: break-word; word-break: break-all; width: 450px; } " //$NON-NLS-1$
                + ".z { display: table-cell; border: solid; border-width: thin; padding: 3px; text-align: left; vertical-align: middle; word-wrap: break-word; word-break: break-all; width: 160px; } " //$NON-NLS-1$
                + ".c { display: table-cell; border: solid; border-width: thin; padding: 3px; text-align: right; vertical-align: middle; word-wrap: break-word;  width: 110px; } " //$NON-NLS-1$
                + ".h { display: table-cell; border: solid; border-width: thin; padding: 3px; text-align: center; vertical-align: middle; word-wrap: break-word; width: 110px; }" //$NON-NLS-1$
                + " TD:hover[onclick]{background-color:#F0F0F0; cursor:pointer} " //$NON-NLS-1$
                + "</style>"); //$NON-NLS-1$
        out.println("</HEAD>"); //$NON-NLS-1$
        out.println("<BODY>"); //$NON-NLS-1$
	}

	public void endDocument(PrintWriter out){
		out.println("</BODY></HTML>"); //$NON-NLS-1$
	}
	
    public byte[] generateSkypeConversationHtml(SkypeConversation c) throws UnsupportedEncodingException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(bout, "UTF-8")); //$NON-NLS-1$
        
        startDocument(out);
        out.println("<TABLE>"); //$NON-NLS-1$

        out.println("<TR><TH>"+Messages.getString("SkypeReport.Attribute")+"</TH><TH>"+Messages.getString("SkypeReport.Value")+"</TH></TR>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        out.println(NEW_ROW+"ID"+NEW_COL+c.getId()+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+"Nome do chat"+NEW_COL+FormatUtil.format(c.getDisplayName())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+"Criado em:"+NEW_COL+FormatUtil.format(c.getCreationDate())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+"Data da última atividade:"+NEW_COL+FormatUtil.format(c.getLastActivity())+CLOSE_ROW); //$NON-NLS-1$
        out.println("</TABLE>"); //$NON-NLS-1$

        out.println("<P>"+Messages.getString("SkypeReport.Messages")+":</P>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        out.println("<TABLE>"); //$NON-NLS-1$
        out.println("<TR>" //$NON-NLS-1$
        		+ "<TH>"+Messages.getString("SkypeReport.ID")+"</TH>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        		+ "<TH>"+Messages.getString("SkypeReport.Date")+"</TH>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        		+ "<TH>"+Messages.getString("SkypeReport.Author")+"</TH>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        		+ "<TH>"+Messages.getString("SkypeReport.Recipient")+"</TH>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        		+ "<TH>"+Messages.getString("SkypeReport.Content")+"</TH>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
/*        		+ "<TH>Enviada?</TH>"
        		+ "<TH>Entregue?</TH>"
*/        		+ "<TH>"+Messages.getString("SkypeReport.EditedBy")+"</TH>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        		+ "<TH>"+Messages.getString("SkypeReport.EditedDate")+"</TH>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        		+ "<TH>"+Messages.getString("SkypeReport.RemoteID")+"</TH>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        		+ "</TR>"); //$NON-NLS-1$
        
        int i=0;
        for(SkypeMessage sm : c.getMessages()) {
        	i++;

        	//boolean destaque=(i%2)==0;
        	boolean destaque=skypeName.equals(sm.getAutor());

            out.println("<TR class='"+ (destaque?"rb":"rr") + "'>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            		+ "<TD>"+sm.getId()+"</TD>" //$NON-NLS-1$ //$NON-NLS-2$
            		+ "<TD>"+FormatUtil.format(sm.getData())+"</TD>" //$NON-NLS-1$ //$NON-NLS-2$
            		+ "<TD>"+FormatUtil.format(sm.getAutor())+"</TD>"); //$NON-NLS-1$ //$NON-NLS-2$
            out.print("<TD>"+FormatUtil.format(sm.getDestino())+"</TD>"); //$NON-NLS-1$ //$NON-NLS-2$
            if(sm.getAnexoUri() != null && sm.getAnexoUri().getThumbFile() != null){
				try (InputStream is = sm.getAnexoUri().getThumbFile().getBufferedStream()){
	            	byte[] b = IOUtil.loadInputStream(is);
	                out.print("<TD>" //$NON-NLS-1$
	                		+ "<img height=\"100\" width=\"100\" src=\"data:image/jpg;charset=utf-8;base64,"+Base64.getEncoder().encodeToString(b)+"\"/>" //$NON-NLS-1$ //$NON-NLS-2$
	                		+ "<div>"+Messages.getString("SkypeReport.ImageCacheMsg")+"</div>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	                		+ "</TD>"); //$NON-NLS-1$
				} catch (IOException e) {
					out.print("<TD></TD>"); //$NON-NLS-1$
					e.printStackTrace();
				}
            }else{
                out.print("<TD>"+FormatUtil.format(sm.getConteudo())+"</TD>"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            
/*            out.print("<TD>"+sm.isEnviada()+"</TD>");
            out.print("<TD>"+sm.isRecebidaNoDestino()+"</TD>");
*/            out.print("<TD>"+FormatUtil.format(sm.getEditor())+"</TD>"); //$NON-NLS-1$ //$NON-NLS-2$
            if(sm.getDataEdicao()!=null){
                out.print("<TD>"+FormatUtil.format(sm.getDataEdicao())+"</TD>"); //$NON-NLS-1$ //$NON-NLS-2$
            }else{
                out.print("<TD> - </TD>"); //$NON-NLS-1$
            }
            out.print("<TD>"+sm.getIdRemoto()+"</TD>"); //$NON-NLS-1$ //$NON-NLS-2$
            out.print("</TR>"); //$NON-NLS-1$
        }
        
        out.println("</TABLE>"); //$NON-NLS-1$

        endDocument(out);
        
        out.flush();
        return bout.toByteArray();
    }

	public byte[] generateSkypeContactHtml(SkypeContact c) throws UnsupportedEncodingException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(bout, "UTF-8")); //$NON-NLS-1$

        startDocument(out);

        try{
            out.println("<P>"+"Dados do usuário:"+FormatUtil.format(c.getSkypeName())+"</P>");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
            byte b[] = c.getAvatar();
            if(b!=null){
                out.println("<P>"+Messages.getString("SkypeReport.Avatar")+" <img src=\"data:image/jpg;charset=utf-8;base64,"+Base64.getEncoder().encodeToString(b)+"\" title=\"File\"/> </P>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
        }catch(Exception e){
        	e.printStackTrace();       	
        }

        out.println("<TABLE>"); //$NON-NLS-1$
        out.println("<TR><TH>"+Messages.getString("SkypeReport.Attribute")+"</TH><TH>"+Messages.getString("SkypeReport.Value")+"</TH></TR>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        out.println(NEW_ROW+Messages.getString("SkypeReport.ID")+NEW_COL+c.getId()+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.SkypeID")+NEW_COL+FormatUtil.format(c.getSkypeName())+CLOSE_ROW); //$NON-NLS-1$ 
        out.println(NEW_ROW+Messages.getString("SkypeReport.ProfileDate")+NEW_COL+FormatUtil.format(c.getProfileDate())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.FullName")+NEW_COL+FormatUtil.format(c.getFullName())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.DisplayName")+NEW_COL+FormatUtil.format(c.getDisplayName())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.BirthDate")+NEW_COL+FormatUtil.format(c.getBirthday())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.City")+NEW_COL+FormatUtil.format(c.getCity())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.About")+NEW_COL+FormatUtil.format(c.getSobre())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.PstnNum")+NEW_COL+FormatUtil.format(c.getPstnNumber())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.Phone")+NEW_COL+FormatUtil.format(c.getAssignedPhone())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.Email")+NEW_COL+FormatUtil.format(c.getEmail())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.AvatarDate")+NEW_COL+FormatUtil.format(c.getAvatarDate())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.LastOnlineDate")+NEW_COL+FormatUtil.format(c.getLastOnlineDate())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.LastContactedDate")+NEW_COL+FormatUtil.format(c.getLastUsed())+CLOSE_ROW); //$NON-NLS-1$
        
        out.println("</TABLE>"); //$NON-NLS-1$

        endDocument(out);

        out.flush();
        return bout.toByteArray();
    }	

    public byte[] generateSkypeMessageHtml(SkypeMessage sm) throws UnsupportedEncodingException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(bout, "UTF-8")); //$NON-NLS-1$

        startDocument(out);

        out.println("<TABLE>"); //$NON-NLS-1$
        out.println("<TR><TH>"+Messages.getString("SkypeReport.Attribute")+"</TH><TH>"+Messages.getString("SkypeReport.Value")+"</TH></TR>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        out.println(NEW_ROW+Messages.getString("SkypeReport.ID")+NEW_COL+sm.getId()+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.Date")+NEW_COL+FormatUtil.format(sm.getData())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.Author")+NEW_COL+FormatUtil.format(sm.getAutor())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.Recipient")+NEW_COL+FormatUtil.format(sm.getDestino())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.Content")+NEW_COL+FormatUtil.format(sm.getConteudo())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.ChatName")+NEW_COL+FormatUtil.format(sm.getConversation().getChatName())+CLOSE_ROW); //$NON-NLS-1$
/*        out.println("<TR><TD>Enviada?</TD><TD>"+sm.isEnviada()+CLOSE_ROW);
        out.println("<TR><TD>Entregue?</TD><TD>"+sm.isRecebidaNoDestino()+CLOSE_ROW);
*/        if(sm.getDataEdicao()!=null){
            out.println(NEW_ROW+Messages.getString("SkypeReport.EditedBy")+NEW_COL+FormatUtil.format(sm.getEditor())+CLOSE_ROW); //$NON-NLS-1$
            out.println(NEW_ROW+Messages.getString("SkypeReport.EditedDate")+NEW_COL+FormatUtil.format(sm.getDataEdicao())+CLOSE_ROW); //$NON-NLS-1$
        }
        out.println(NEW_ROW+Messages.getString("SkypeReport.RemoteID")+NEW_COL+sm.getIdRemoto()+CLOSE_ROW); //$NON-NLS-1$
        out.println("</TABLE>"); //$NON-NLS-1$
        
        if(sm.getAnexoUri() != null && sm.getAnexoUri().getThumbFile() != null){
        	try (InputStream is = sm.getAnexoUri().getThumbFile().getBufferedStream()){
        		byte[] b = IOUtil.loadInputStream(is);
                out.print(Messages.getString("SkypeReport.ImageCacheMsg")+":<br/><img src=\"data:image/jpg;charset=utf-8;base64,"+Base64.getEncoder().encodeToString(b)+"\"/></TD>");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

        endDocument(out);
        
        out.flush();
        return bout.toByteArray();
        
    }


	public byte[] generateSkypeTransferenciaHtml(SkypeFileTransfer c, ItemSearcher searcher) throws UnsupportedEncodingException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(bout, "UTF-8")); //$NON-NLS-1$
        
        startDocument(out);
        out.println("<TABLE>"); //$NON-NLS-1$
        out.println("<TR><TH>"+Messages.getString("SkypeReport.Attribute")+"</TH><TH>"+Messages.getString("SkypeReport.Value")+"</TH></TR>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        out.println(NEW_ROW+Messages.getString("SkypeReport.TypeDescr")+NEW_COL+c.getTypeDescr()+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.ID")+NEW_COL+c.getId()+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.FileName")+NEW_COL+FormatUtil.format(c.getFilename())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.FullPath")+NEW_COL+FormatUtil.format(c.getFilePath())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.From")+NEW_COL+FormatUtil.format(c.getFrom())+CLOSE_ROW); //$NON-NLS-1$
        if(c.getType() == 3){
            out.println(NEW_ROW+Messages.getString("SkypeReport.Recipient")+NEW_COL+Messages.getString("SkypeReport.Chat")+": "+c.getConversation().getId()+" -- "+c.getConversation().getDisplayName()+"<BR/>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            
            out.println(Messages.getString("SkypeReport.Participants")+"<br/>"); //$NON-NLS-1$ //$NON-NLS-2$
            if(c.getConversation().getParticipantes()!=null){
                for (Iterator iterator = c.getConversation().getParticipantes().iterator(); iterator.hasNext();) {
    				String part = (String) iterator.next();
    	            out.println(part+"<br/>"); //$NON-NLS-1$
    			}
            }
            
            out.println(CLOSE_ROW);
        }else{
            out.println(NEW_ROW+Messages.getString("SkypeReport.Recipient")+NEW_COL+FormatUtil.format(c.getTo())+CLOSE_ROW); //$NON-NLS-1$
        }

        out.println(NEW_ROW+Messages.getString("SkypeReport.StartDate")+NEW_COL+FormatUtil.format(c.getStart())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.AcceptDate")+NEW_COL+FormatUtil.format(c.getAccept())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.FinishDate")+NEW_COL+FormatUtil.format(c.getFinish())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.FileSize")+NEW_COL+c.getFileSize()+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.SentBytes")+NEW_COL+c.getBytesTransferred()+CLOSE_ROW); //$NON-NLS-1$
        if(c.getConversation()!=null){
        	out.println(NEW_ROW+Messages.getString("SkypeReport.ChatID")+NEW_COL+c.getConversation().getId()+" -- "+c.getConversation().getDisplayName()+CLOSE_ROW); //$NON-NLS-1$ //$NON-NLS-2$
        }else{
        	out.println(NEW_ROW+Messages.getString("SkypeReport.ChatID")+NEW_COL+Messages.getString("SkypeReport.Empty")+CLOSE_ROW); //$NON-NLS-1$ //$NON-NLS-2$
        }
        out.println("</TABLE>"); //$NON-NLS-1$
        
        insertLinkAndThumb(out, c, searcher);

        endDocument(out);
        
        out.flush();
        return bout.toByteArray();
    }
	
	private void insertLinkAndThumb(PrintWriter out, SkypeFileTransfer c, ItemSearcher searcher){
		
        ItemBase item = c.getItem();
    	if(item != null){
    		String query = c.getItemQuery();
    		out.println("<p>"+Messages.getString("SkypeReport.LikelyFile"));  //$NON-NLS-1$ //$NON-NLS-2$
            out.println("<a onclick=\"app.open(" + SimpleHTMLEncoder.htmlEncode("\"" + query.replace("\"", "\\\"") + "\"") + ")\" "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
            List<ItemBase> result = Util.getItems(query, searcher);
            if(result != null && !result.isEmpty()) {
                String exportPath = Util.getExportPath(result.get(0));
                if(!exportPath.isEmpty())
                    out.println("href=\"" + exportPath + "\""); //$NON-NLS-1$ //$NON-NLS-2$    
            }
        	out.println(">" + SimpleHTMLEncoder.htmlEncode(query)); //$NON-NLS-1$
    		byte[] thumb = Util.getPreview(item);
    		if(thumb != null)
    			out.println("<br><img height=\"300\" width=\"300\" src=\"data:image/jpg;charset=utf-8;base64,"+Base64.getEncoder().encodeToString(thumb)+"\"/>"); //$NON-NLS-1$ //$NON-NLS-2$
    		out.println("</a></p>"); //$NON-NLS-1$
    	}
	}

	public byte[] generateSkypeAccountHtml(SkypeAccount a, int contactCount, int transferCount, int msgCount) throws UnsupportedEncodingException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(bout, "UTF-8")); //$NON-NLS-1$

        startDocument(out);

        try{
            out.println("<P>"+Messages.getString("SkypeReport.UserData")+FormatUtil.format(a.getSkypeName())+"</P>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            byte b[] = a.getAvatar();
            if(b!=null){
                out.println("<P>"+Messages.getString("SkypeReport.Avatar")+" <img src=\"data:image/jpg;charset=utf-8;base64,"+Base64.getEncoder().encodeToString(b)+"\" title=\"File\"/> </P>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
        }catch(Exception e){
        	e.printStackTrace();       	
        }

        out.println("<TABLE>"); //$NON-NLS-1$
        out.println("<TR><TH>"+Messages.getString("SkypeReport.Attribute")+"</TH><TH>"+Messages.getString("SkypeReport.Value")+"</TH></TR>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        out.println(NEW_ROW+Messages.getString("SkypeReport.ID")+NEW_COL+a.getId()+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.Mood")+NEW_COL+a.getMood()+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.MoodDate")+NEW_COL+FormatUtil.format(a.getMoodTimestamp())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.AvatarDate")+NEW_COL+FormatUtil.format(a.getAvatarTimestamp())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.SkypeID")+NEW_COL+FormatUtil.format(a.getSkypeName())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.ProfileDate")+NEW_COL+FormatUtil.format(a.getProfileTimestamp())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.FullName")+NEW_COL+FormatUtil.format(a.getFullname())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.BirthDate")+NEW_COL+FormatUtil.format(a.getBirthday())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.About")+NEW_COL+FormatUtil.format(a.getAbout())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.Country")+NEW_COL+FormatUtil.format(a.getCountry())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.Province")+NEW_COL+FormatUtil.format(a.getProvince())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.City")+NEW_COL+FormatUtil.format(a.getCity())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.Email")+NEW_COL+FormatUtil.format(a.getEmail())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.HomePhone")+NEW_COL+FormatUtil.format(a.getPhoneHome())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.OfficePhone")+NEW_COL+FormatUtil.format(a.getPhoneOffice())+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.MobilePhone")+NEW_COL+FormatUtil.format(a.getPhoneMobile())+CLOSE_ROW); //$NON-NLS-1$
        out.println("</TABLE>"); //$NON-NLS-1$
        out.println("<BR>"); //$NON-NLS-1$
        out.println("<TABLE>"); //$NON-NLS-1$
        out.println("<TR><TH>"+Messages.getString("SkypeReport.TotalCount")+"</TH></TR>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        out.println(NEW_ROW+Messages.getString("SkypeReport.ContactCount")+contactCount+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.MessageCount")+msgCount+CLOSE_ROW); //$NON-NLS-1$
        out.println(NEW_ROW+Messages.getString("SkypeReport.TransferCount")+transferCount+CLOSE_ROW); //$NON-NLS-1$
        out.println("</TABLE>"); //$NON-NLS-1$

        
        endDocument(out);

        out.flush();
        return bout.toByteArray();
    }	

    
}