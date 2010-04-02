/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.layout;

import com.nativelibs4java.opencl.*;

import static com.nativelibs4java.opencl.OpenCL4Java.*;
import com.nativelibs4java.util.NIOUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import processing.core.PApplet;
import tinaviz.filters.NodeList;
import tinaviz.graph.Node;
import tinaviz.view.View;

/**
 * Hardware-accelerated layout engine
 * 
 * @author Julian Bilcke
 */
public class LayoutOpenCL extends Layout {

    private CLContext context;
    private CLQueue queue;
    private CLFloatBuffer input;
    private CLFloatBuffer output;
    private CLProgram program;
    private final CLKernel kernel;
    private CLEvent kernelCompletion;
    private int dataSize = 0;
    private String kernelPath = "forceVectorLayout.c";

    public LayoutOpenCL() throws CLBuildException, IOException {
        context = JavaCL.createBestContext();
        queue = context.createDefaultQueue();

        dataSize = 128;

        input = context.createFloatBuffer(CLMem.Usage.Input, dataSize);
        output = context.createFloatBuffer(CLMem.Usage.Output, dataSize);

        String sources = "__kernel void  myKernel() {}";

        //sources = loadKernelFile(kernelPath);

        program = context.createProgram(sources).build();
        kernel = program.createKernel("myKernel");

    }

    private String loadKernelFile(String filePath) throws java.io.IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(filePath)));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }

    @Override
    public void fast(View v, NodeList nodes) {
        float distance = 1f;
        float vx = 1f;
        float vy = 1f;

        /*
        float repulsion = v.repulsion;
        float attraction = v.attraction;

        float gravity = 0.00001f;

        // The same kernel can be safely used by different threads, as long as setArgs + enqueueNDRange are in a synchronized block
        synchronized (kernel) {
            // setArgs will throw an exception at runtime if the types / sizes of the arguments are incorrect
            kernel.setArgs(input, output, gravity);

            // Ask for 1-dimensional execution of length dataSize, with auto choice of local workgroup size :
            kernelCompletion = kernel.enqueueNDRange(queue, new int[]{dataSize}, null);
        }
        kernelCompletion.waitFor(); // better not to wait for it but to pass it as a dependent event to some other queuable operation (CLBuffer.read, for instance)

*/
    }
}
