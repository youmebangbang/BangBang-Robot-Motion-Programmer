package BangBangMotion;

import java.io.File;

import com.interactivemesh.jfx.importer.stl.StlMeshImporter;

import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;

public class ModelViewer {

	private static final String MESH_FILENAME = "/Users/Captain/Desktop/pcbtray.stl";

	private static final double MODEL_SCALE_FACTOR = 100;
	private static final double MODEL_X_OFFSET = 0; // standard
	private static final double MODEL_Y_OFFSET = 0; // standard

	private static final int VIEWPORT_SIZE = 800;

	private static final Color lightColor = Color.rgb(244, 255, 250);
	private static final Color jewelColor = Color.rgb(0, 190, 222);

	private Group root;
	private PointLight pointLight;
	
	public MeshView[] buildMesh()
	{
		File file = new File(MESH_FILENAME);
		if(file.exists()) {
			System.out.println("stl file found");
		}
	    StlMeshImporter importer = new StlMeshImporter();
	    importer.read(file);
	    Mesh mesh = importer.getImport();
	    MeshView[] meshArray = { new MeshView(mesh) };
	    meshArray[0].setMaterial(new PhongMaterial(Color.rgb(0, 190, 222)));	   

	   // Color ambientColor = Color.rgb(80, 80, 80, 0);
	   // AmbientLight ambient = new AmbientLight(ambientColor);
	    
	    return meshArray;
	    
	    
	}

}
