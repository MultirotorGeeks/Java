package pmrancuret.uncrustify.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import pmrancuret.uncrustify.Activator;
import pmrancuret.uncrustify.preferences.PreferenceConstants;

public class PreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public PreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Configure the options and paths for uncrustify");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		addField ( new FileFieldEditor ( PreferenceConstants.P_PATH, 
				                         "&Uncrustify path:", 
				                         getFieldEditorParent ( ) ) );
		addField ( new FileFieldEditor ( PreferenceConstants.P_CONFIG, 
				                         "&Configuration file:", 
				                         getFieldEditorParent ( ) ) );
		addField ( new BooleanFieldEditor ( PreferenceConstants.P_INPLACE,
				                            "Modify files in-place (--no-backup)",
				                            getFieldEditorParent ( ) ) );
		addField ( new StringFieldEditor ( PreferenceConstants.P_EXT,
				                           "&Extension list (comma-separated):",
				                           getFieldEditorParent ( ) ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
}
