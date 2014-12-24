package pmrancuret.uncrustify.handlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;

import pmrancuret.uncrustify.Activator;
import pmrancuret.uncrustify.preferences.*;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class handleCrust extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public handleCrust() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException
	{

		/* Get the generic "selection" */
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow ( event ).getSelectionService ( ).getSelection ( );
		
		/* This if / else if tree handles which type of selection was detected */
		if ( selection instanceof IStructuredSelection )
		{
			
			/****************************************************/
			/**  This indicates selections come from explorer  **/
			/**  or navigator type tree view of files          **/
			/****************************************************/

			/* Create a string array in case we have files we didn't process */
			String badFiles = new String ( );
			
			/* Create an object array and parse through it */
	        Object selections [ ] = ( (IStructuredSelection) selection ).toArray ( );
	        
	        for ( Object obj : selections )
	        {
	        	
	        	/* Get an IResource from the object */
	        	IResource file = getFile ( obj );
	        	
	        	/* Check to see if we should process this file */
	        	if ( ! shouldProcessFile ( file ) )
        		{
	        		if ( selections.length > 1 )
        			{
	        			continue;
        			}
	        		else
					{
						MessageDialog.openInformation ( PlatformUI.getWorkbench ( ).getActiveWorkbenchWindow ( ).getShell ( ),
		                                                "Incompatible File",
		                                                "This file type is not supported based on current Uncrustify configuration." );
						continue;
					}
        		}
	        	
	        	/* Dirty files skipped */
	        	if ( fileDirty ( file.getFullPath ( ).toString ( ) ) )
	        	{
	        		badFiles += "\t" + file.getName ( ) + "\n";
	        	}
	        	else
	        	{
		        	/* Process the file if we're still here */
		        	processFile ( file );
		        	
		        	/* Refresh any affected editor window */
		        	refreshEditor ( file.getFullPath ( ).toString ( ) );
	        	}
	        }
	        
	        /* Inform user of dirty files if any */
	        if ( badFiles.length ( ) > 0 )
	        {
	        	MessageDialog.openInformation ( PlatformUI.getWorkbench ( ).getActiveWorkbenchWindow ( ).getShell ( ),
	        			                        "Dirty Files",
	        			                        "Beuatification was not performed on the following unsaved files:\n\n" +
	        			                        badFiles + "\nSave the files and try again if desired.");
	        }
	        
	    }
		else if ( selection instanceof TextSelection )
		{
			
			/********************************************************/
			/**  This indicates the selection came from an editor  **/
			/********************************************************/
			
			/* Get filename and check to see if we ought to run it */
			IEditorPart activeEdit = PlatformUI.getWorkbench ( ).getActiveWorkbenchWindow ( ).getActivePage ( ).getActiveEditor ( );
			String      fileName   = activeEdit.getEditorInput ( ).getName ( );
			
			
			/* Get selected text */
			TextSelection txtSel = (TextSelection) selection;
			
			if ( txtSel.getLength ( ) == 0 )
			{
				/* No text - must want entire file done up */
				IResource file = getFile ( (Object) activeEdit.getEditorInput ( ) );
				
				/* See if this file fits the bill and prompt user */
				if ( shouldProcessFile ( file ) )
				{
					if ( fileDirty ( file.getFullPath ( ).toString ( ) ) )
					{
						MessageDialog.openInformation ( PlatformUI.getWorkbench ( ).getActiveWorkbenchWindow ( ).getShell ( ),
		                        "Dirty File",
		                        "File must be saved prior to beautification." );
					}
					else if ( MessageDialog.openConfirm ( PlatformUI.getWorkbench ( ).getActiveWorkbenchWindow ( ).getShell ( ),
						                                  "Beautify File", 
						                                  "Entire file " + fileName + " will be beautified." ) )
					{
						
						/* User pressed OK, so process the file */
			        	processFile ( file );
			        	
			        	/* Refresh any affected editor window */
			        	refreshEditor ( file.getFullPath ( ).toString ( ) );
						
					}
				}
				else
				{
					MessageDialog.openInformation ( PlatformUI.getWorkbench ( ).getActiveWorkbenchWindow ( ).getShell ( ),
	                                                "Incompatible File",
	                                                "This file type is not supported based on current Uncrustify configuration." );
				}
			}
			else
			{
				/* Only a portion of text is selected, not the whole file */
				IDocument doc = ( (ITextEditor) activeEdit ).getDocumentProvider ( ).getDocument ( activeEdit.getEditorInput ( ) );
				try
				{
					doc.replace ( txtSel.getOffset ( ), txtSel.getLength ( ), processText ( txtSel.getText ( ) ) );
				}
				catch (BadLocationException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}
	
	
	
	private boolean shouldProcessFile ( IResource file )
	{
		
		/* Initialize return to false */
		boolean retVal = false;

        /* Skip null references */
        if ( file != null )
        {
		
			/* Get preferences */
	        IPreferenceStore uncrustPref = Activator.getDefault().getPreferenceStore();
	        
	        /* Grab extension list */
	        String extArray [] = uncrustPref.getString ( PreferenceConstants.P_EXT ).split ( "," );

        	/* Check extension match */
        	for ( String ext : extArray )
        	{
        		if ( ext.equalsIgnoreCase ( file.getFileExtension ( ) ) )
        		{
        			retVal = true;
        			break;
        		}
        	}
        }
        
		
		/* Return */
		return retVal;
		
	}
	
	
	
	private IResource getFile ( Object obj )
	{
		
		/* Cast the object to an IResource and make sure we're good! */
		IResource file = (IResource) Platform.getAdapterManager ( ).getAdapter ( obj, IResource.class );
		 
        if ( file == null )
        {
        	if ( obj instanceof IAdaptable ) 
            {
                file = (IResource) ( (IAdaptable) obj ).getAdapter ( IResource.class );
            }
        }
		
		/* Return */
		return file;
		
	}
	
	
	
	private void processFile ( IResource file )
	{
		
		/* Get preferences */
        IPreferenceStore uncrustPref = Activator.getDefault().getPreferenceStore();

        
        /* Build the command line */
        String argVal = uncrustPref.getString ( PreferenceConstants.P_PATH );
        
        /* Set quiet mode (no stderr) and add config file */
        argVal = argVal.concat ( " -q -c " );
        argVal = argVal.concat ( uncrustPref.getString ( PreferenceConstants.P_CONFIG ) );
		
        /* Check for in-place modification */
        if ( uncrustPref.getBoolean ( PreferenceConstants.P_INPLACE ) )
        {
        	argVal = argVal.concat ( " --no-backup " );
        }
        else
        {
        	argVal = argVal.concat ( " --replace " );
        }
        
        /* Complete the argument line */
    	argVal = argVal.concat( file.getLocation ( ).toFile ( ).getAbsolutePath ( ) );
    	

    	/* Try / catch for process operations */
		try
		{

			/* Run beautifier and wait for completion */
			Process proc = Runtime.getRuntime ( ).exec ( argVal );
			proc.waitFor ( );
			
		}
		catch ( IOException e )
		{
			e.printStackTrace ( );
		}
		catch (InterruptedException e)
		{
			e.printStackTrace ( );
		}
		
	}
	
	
	
	private String processText ( String oldText )
	{
		
		/* Setup return text */
		String newText = new String ( );

		/* Get preferences */
        IPreferenceStore uncrustPref = Activator.getDefault().getPreferenceStore();

        
        /* Build the command line */
        String argVal = uncrustPref.getString ( PreferenceConstants.P_PATH );
        
        
        /* HARD-CODE TO C LANGUAGE */
        argVal = argVal.concat ( " -l C " );
        
        /* Set quiet mode (no stderr) and add config file */
        argVal = argVal.concat ( " -q -c " );
        argVal = argVal.concat ( uncrustPref.getString ( PreferenceConstants.P_CONFIG ) );
		

    	/* Try / catch for process operations */
		try
		{

			/* Run beautifier and feed it input */
			Process proc = Runtime.getRuntime ( ).exec ( argVal );
			
			/* Setup streams */
			BufferedWriter writeToProc  = new BufferedWriter ( new OutputStreamWriter ( proc.getOutputStream ( ) ) );
			BufferedReader readFromProc = new BufferedReader ( new InputStreamReader  ( proc.getInputStream  ( ) ) );
			
			/* Write selected text */
			writeToProc.write( oldText );
			writeToProc.flush ( );
			writeToProc.close ( );
			
			
			/* Collect output */
			int x;
			while ( ( x = readFromProc.read ( ) ) != -1 )
			{
				newText += (char) x;
			}
			readFromProc.close ( );
			
			/* Wait for completion */
			proc.waitFor ( );
			
		}
		catch ( IOException e )
		{
			e.printStackTrace ( );
		}
		catch (InterruptedException e)
		{
			e.printStackTrace ( );
		}


		/* Return */
		return newText;
		
	}
	
	
	
	private boolean fileDirty ( String filePath )
	{

		/* Get dirty editors */
		IEditorPart editors [ ] = PlatformUI.getWorkbench ( ).getActiveWorkbenchWindow ( ).getActivePage ( ).getDirtyEditors ( );
		
		/* If any are dirty, parse them to find out if the file is open */
		if ( editors != null )
		{
			for ( IEditorPart editor : editors )
			{
				if ( ( editor.getEditorInput ( ).getAdapter ( IResource.class ) != null ) &&
					 ( filePath.equals ( ( (IResource) editor.getEditorInput ( ).getAdapter ( IResource.class ) ).getFullPath ( ).toString ( ) ) ) )
				{
					return true;
				}
			}
		}
		
	    /* Return */
		return false;
	}
	
	
	private void refreshEditor ( String filePath )
	{
		/* Get active editor */
		IEditorPart activeEdit = PlatformUI.getWorkbench ( ).getActiveWorkbenchWindow ( ).getActivePage ( ).getActiveEditor ( );
		
		/* Refresh editor */
		IEditorReference editors [ ] = PlatformUI.getWorkbench ( ).getActiveWorkbenchWindow ( ).getActivePage ( ).getEditorReferences ( );
		for ( IEditorReference editor : editors )
		{
			try 
			{
				if ( ( editor.getEditorInput ( ).getAdapter ( IResource.class ) != null ) &&
				     ( filePath.equals ( ( (IResource) editor.getEditorInput ( ).getAdapter ( IResource.class ) ).getFullPath ( ).toString ( ) ) ) )
				{
					PlatformUI.getWorkbench ( ).getActiveWorkbenchWindow ( ).getActivePage ( ).activate ( editor.getPart ( false ) );
				}
				
			}
			catch (PartInitException e)
			{
				e.printStackTrace();
			}
		}
		
	    /* Restore active editor */
		PlatformUI.getWorkbench ( ).getActiveWorkbenchWindow ( ).getActivePage ( ).activate ( activeEdit );
	}
}
