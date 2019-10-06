import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Random;

/**
* Simple maze generator implemented to create
* all positions availables in html & css.
* Just a funny game owo !
*
* @author  yka yudakan
* @version 1.0
* @since   2019-10-06
*/

public class Maze {

    public static void main(String[] args) {
        
        // Generate matrix maze
        boolean[][] maze = generator(40, 28);
        basicPrint(' ', '#', maze);

        // Create all positions in html
        
        File template = new File("maze-template.html");
        new File("../maze").mkdir();

        for (int i=0; i < maze.length; i++)
            for (int j=0; j < maze[i].length; j++)
                if (maze[i][j]) {

                    try {
                        File fileHtml = new File( "../maze/" + ((i << 16) | j) + ".html" );
                        OutputStreamWriter outFile = new OutputStreamWriter (new FileOutputStream( fileHtml ));
                        outFile.write(
                                makeHtml(
                                    template,
                                    new int[] {i, j},
                                    new int[] {maze.length-2, maze[0].length-2},
                                    maze
                                )
                            );
                            
                        outFile.flush();
                        outFile.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }

    }

    public static String makeHtml(File template, int posPlayer[], int posWinner[], boolean maze[][]) {

        StringBuilder templateSB = new StringBuilder();
        StringBuilder mazeSB = new StringBuilder();
        
        // Read template file
        try {
            InputStreamReader templateIn = new InputStreamReader(new FileInputStream(template));
            int ch = 0;
            while ((ch = templateIn.read()) != -1)
                templateSB.append((char)(ch));
            
            templateIn.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        // Win?
        if (posPlayer[0] == posWinner[0] && posPlayer[1] == posWinner[1]) {
            mazeSB.append("<div id=\"win\"></div>");
            mazeSB.append("<h2>You Win!</h2>");
        }

        // Draw maze
        mazeSB.append("<div id=\"maze\">");
        for (int i=0; i < maze.length; i++) {

            mazeSB.append("<div class=\"flex-row\">"); // Row

            for (int j=0; j < maze[i].length; j++) { // Column
                
                if (posPlayer[0] == i && posPlayer[1] == j) {
                    mazeSB.append("<div class=\"flex-col player\"></div>");

                    // Controls
                    String[] controls = {
                        maze[i-1][j] ? "./" + String.valueOf((i-1 << 16) | j) + ".html" : "#",
                        maze[i][j-1] ? "./" + String.valueOf((i << 16) | j-1) + ".html" : "#",
                        maze[i][j+1] ? "./" + String.valueOf((i << 16) | j+1) + ".html" : "#",
                        maze[i+1][j] ? "./" + String.valueOf((i+1 << 16) | j) + ".html" : "#"
                    };

                    for (String str: controls) {
                        int index = templateSB.indexOf("%s");
                        templateSB = templateSB.replace(index, index+2, str);
                    }
                }
                else if (posWinner[0] == i && posWinner[1] == j)
                    mazeSB.append("<div class=\"flex-col winner\"></div>");
                else
                    mazeSB.append("<div class=\"flex-col "+ (maze[i][j] ? "true" : "false") +"\"></div>");
            }

            mazeSB.append("</div>");
        }
        mazeSB.append("</div>");

        int index = templateSB.indexOf("%s");
        return templateSB.replace(index, index+2, mazeSB.toString()).toString();
    }

    public static void basicPrint(char t, char f, boolean maze[][]) {
        for (int i=0; i < maze.length; i++)
            for (int j=0; j < maze[i].length; j++)
                System.out.print(j < maze[i].length-1 ? (maze[i][j] ? t+" " : f+" ") : (maze[i][j] ? t+"\n" : f+"\n"));
    }

    // Maze Generator based on "Aldous-Broder algorithm"
    // That's an slow one, but easy and funny to do it o3o
    public static boolean[][] generator(int w, int h) {

        // Too large for codec
        if (w >= 65536 || h >= 65536) return null;

        // VARS
        Random rand = new Random();
        ArrayList<Integer> starts = new ArrayList<>();
        ArrayList<Integer> ends = new ArrayList<>();
        
        int x1, x2;
        int y1, y2;
        int pos1, pos2;
        
        int actual = 1;
        int all = w * h;

        // First random cell
        x2 = rand.nextInt(w);
        y2 = rand.nextInt(h);
        pos2 = (x2 << 16) | y2;

        // Neighbor cell
        while (actual < all) {

            // Save last step
            pos1 = pos2;
            x1 = x2;
            y1 = y2;

            // New step
            int dir = rand.nextInt(4);
            switch (dir) {
                case 0:
                    y2 = y2-1 >= 0 ? y2-1 : y2+1;
                    break;
                case 1:
                    x2 = x2+1 < w ? x2+1 : x2-1;
                    break;
                case 2:
                    y2 = y2+1 < h ? y2+1 : y2-1;
                    break;
                case 3:
                    x2 = x2-1 >= 0 ? x2-1 : x2+1;
                    break;
            }
            pos2 = (x2 << 16) | y2;

            // Check if this position is already checked
            if (starts.contains(pos2) || ends.contains(pos2))
                continue;

            // Add path
            starts.add(pos1);
            ends.add(pos2);

            actual++;
        }

        // Generate maze maze
        // x2 because of walls
        // +1 because of maze limits
        boolean[][] maze = new boolean[h*2+1][w*2+1];
        
        for (int i=0; i < starts.size(); i++) {
            x1 = (starts.get(i) >>> 16) * 2;
            y1 = (starts.get(i) & 0xFFFF) * 2;
            maze[y1+1][x1+1] = true;

            x2 = (ends.get(i) >>> 16) * 2;
            y2 = (ends.get(i) & 0xFFFF) * 2;
            maze[y2+1][x2+1] = true;

            maze[y1 == y2 ? y1+1 : (y1+y2)/2+1][x1 == x2 ? x1+1 : (x1+x2)/2+1] = true;
        }

        return maze;
    }
}
