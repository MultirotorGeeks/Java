package pmrancuret.uncrustify.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import pmrancuret.uncrustify.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_INPLACE, true);
		store.setDefault(PreferenceConstants.P_PATH,    "C:\\Program Files (x86)\\uncrustify-0.60-win32\\uncrustify.exe");
		store.setDefault(PreferenceConstants.P_CONFIG,  "C:\\Program Files (x86)\\uncrustify-0.60-win32\\custom.cfg");
		store.setDefault(PreferenceConstants.P_EXT,     "c,cpp,h");
	}

}