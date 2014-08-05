package gr.watchful.permchecker.panels;

import gr.watchful.permchecker.datastructures.ForgeType;
import gr.watchful.permchecker.datastructures.Globals;
import gr.watchful.permchecker.datastructures.ModPack;
import gr.watchful.permchecker.datastructures.UsesPack;
import gr.watchful.permchecker.utils.FileUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class UpdatePanel extends JPanel implements ActionListener, UsesPack {
	private LabelField packName;
	private FileSelector selector;


    public UpdatePanel() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        packName = new LabelField("Pack Name");
        packName.lock("Currently opened pack");
        this.add(packName);

        selector = new FileSelector("Zip", -1, "zip");
		selector.addListener(this);
        this.add(selector);

        LabelField version = new LabelField("Version");
        this.add(version);

		JButton exportButton = new JButton("Export");
		exportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportPack();
			}
		});
		this.add(exportButton);
    }

	public void setPack(ModPack pack) {
		packName.setText(pack.name);
	}

	public void extractPack(File file) {
		if(!file.exists()) {
			System.out.println("Can't extract pack, file doesn't exist!");
			return;
		}

		int i = file.getName().lastIndexOf('.');
		String ext = "file";
		if(i >= 0) {
			ext = file.getName().substring(i+1);
		}
		if(!ext.equals("zip")) {
			System.out.println("Can't extract pack, file isn't a zip");
			return;
		}

		FileUtils.extractZipTo(file, Globals.getInstance().preferences.workingFolder);
	}

	/**
	 * This triggers all the actions necessary to export the pack in the working folder
	 * Check permissions and create perm file
	 *  - Needs pack folder. From globals
	 *  - Needs mod permissions. Pass modpack
	 *  * Cancel if incorrect permissions
	 * Add libs. This can be just the JSON, or the json and libraries folder
	 *  - Needs pack folder. From globals
	 *  - Needs forge version. Pass modpack
	 * Build xml
	 *  - Needs export folder. From globals
	 *  - Needs modpack. Pass modpack
	 * Zip pack
	 *  - Needs pack folder. From globals
	 *  - Needs export folder. From globals
	 *  - Needs version and shortname. Pass modpack
	 * Upload pack and zip
	 *  - Needs export folder. From globals
	 * Trigger pack json save
	 */
	public void exportPack() {
		boolean success = true;
		if(Globals.getModPack().forgeType.equals(ForgeType.VERSION)) {
			success = FileUtils.addForge(Globals.getInstance().preferences.workingFolder, Globals.getModPack().ForgeVersion);
		} else {
			success = FileUtils.addForge(Globals.getInstance().preferences.workingFolder, Globals.getModPack().forgeType);
		}
		if(!success) {
			System.out.println("pack.json add failed");
			return;
		}
		String xml = FileUtils.buildXML(Globals.getModPack());
		if(!FileUtils.writeFile(xml, new File(
				Globals.getInstance().preferences.exportFolder+File.separator+"static"+
				File.separator+Globals.getModPack().key+".xml"))) {
			System.out.println("xml export failed");
			return;
		}
		FileUtils.zipFilesTo(Globals.getInstance().preferences.workingFolder.listFiles(),
				new File(Globals.getInstance().preferences.exportFolder+File.separator+
						"privatepacks"+File.separator+Globals.getModPack().shortName+File.separator+
						Globals.getModPack().recommendedVersion.replaceAll("\\.","_")+
				File.separator+Globals.getModPack().shortName+".zip"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		extractPack(selector.getFile());
	}

	@Override
	public void updatePack(ModPack modPack) {
		setPack(modPack);
	}
}
