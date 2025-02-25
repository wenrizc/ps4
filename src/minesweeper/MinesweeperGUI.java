package minesweeper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class MinesweeperGUI extends JFrame {
    
    private static final int CELL_SIZE = 30; // 每个格子的大小
    
    private JButton[][] buttons; // 按钮网格
    private final JLabel statusLabel; // 状态标签
    private final JPanel boardPanel; // 游戏面板
    
    private int rows; // 行数
    private int cols; // 列数
    private Socket socket; // 服务器连接
    private BufferedReader in; // 输入流
    private PrintWriter out; // 输出流
    private boolean gameOver = false; // 游戏是否结束
    
    /**
     * 创建扫雷游戏GUI客户端
     * 
     * @param host 服务器地址
     * @param port 服务器端口
     * @throws IOException 连接异常
     */
    public MinesweeperGUI(String host, int port) throws IOException {
        // 初始假设的棋盘大小，连接后会更新
        this.rows = 10;
        this.cols = 10;
        
        // 设置窗口属性
        setTitle("扫雷客户端");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 创建状态栏
        statusLabel = new JLabel("连接中...");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // 创建游戏面板
        boardPanel = new JPanel(new GridLayout(rows, cols));
        buttons = new JButton[rows][cols];
        
        // 初始化按钮
        initializeButtons();
        
        // 布局
        setLayout(new BorderLayout());
        add(boardPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        
        // 连接到服务器并启动通信线程
        connectToServer(host, port);
        
        // 设置窗口大小并显示
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    /**
     * 初始化游戏按钮
     */
    private void initializeButtons() {
        boardPanel.removeAll();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                final int finalX = x;
                final int finalY = y;
                buttons[y][x] = new JButton();
                buttons[y][x].setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
                buttons[y][x].setMargin(new Insets(0, 0, 0, 0));
                buttons[y][x].setFont(new Font("Arial", Font.BOLD, 16));
                
                // 添加左键点击（挖掘）事件
                buttons[y][x].addActionListener(e -> {
                    if (!gameOver) {
                        sendCommand("dig " + finalX + " " + finalY);
                    }
                });
                
                // 添加右键点击（标记/取消标记）事件
                buttons[y][x].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON3 && !gameOver) {
                            String buttonText = buttons[finalY][finalX].getText();
                            if ("F".equals(buttonText)) {
                                sendCommand("deflag " + finalX + " " + finalY);
                            } else if (buttonText.isEmpty() || "-".equals(buttonText)) {
                                sendCommand("flag " + finalX + " " + finalY);
                            }
                        }
                    }
                });
                
                boardPanel.add(buttons[y][x]);
            }
        }
        boardPanel.revalidate();
        boardPanel.repaint();
    }
    
    /**
     * 连接到服务器
     */
    private void connectToServer(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        
        // 启动接收服务器消息的线程
        new Thread(this::receiveMessages).start();
    }
    
    /**
     * 发送命令到服务器
     */
    private void sendCommand(String command) {
        out.println(command);
    }
    
    /**
     * 接收并处理服务器消息
     */
    private void receiveMessages() {
        try {
            String line = in.readLine();
            
            // 处理欢迎消息，提取棋盘大小
            if (line != null && line.startsWith("Welcome")) {
                statusLabel.setText(line);
                
                // 尝试从欢迎消息中提取棋盘大小
                try {
                    String boardInfo = line.split("Board:")[1].split("\\.")[0].trim();
                    String[] dimensions = boardInfo.split("by");
                    cols = Integer.parseInt(dimensions[0].trim().split(" ")[0]);
                    rows = Integer.parseInt(dimensions[1].trim().split(" ")[0]);
                    
                    // 重新创建按钮网格
                    buttons = new JButton[rows][cols];
                    boardPanel.setLayout(new GridLayout(rows, cols));
                    initializeButtons();
                    pack();
                    
                    // 发送look命令获取初始棋盘
                    sendCommand("look");
                } catch (Exception e) {
                    System.err.println("解析棋盘大小失败: " + e.getMessage());
                }
            }
            
            // 持续接收消息
            while ((line = in.readLine()) != null) {
                if (line.equals("bye")) {
                    statusLabel.setText("已断开连接");
                    break;
                } else if (line.equals("BOOM!")) {
                    statusLabel.setText("踩到地雷了！游戏结束");
                    gameOver = true;
                    sendCommand("look"); // 获取最终棋盘
                } else {
                    // 假设是棋盘数据
                    updateBoard(line);
                }
            }
        } catch (IOException e) {
            statusLabel.setText("连接错误: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
   /**
 * 根据服务器返回的数据更新棋盘显示
 */
private void updateBoard(String boardData) {
    SwingUtilities.invokeLater(() -> {
        try {
            String[] lines = boardData.split("\r\n");
            for (int y = 0; y < lines.length; y++) {
                String line = lines[y];
                // 使用正则表达式处理连续的空格
                String[] cells = line.split("\\s+");
                
                // 如果第一个元素是空字符串（行首有空格），则跳过
                int startIdx = cells.length > 0 && cells[0].isEmpty() ? 1 : 0;
                
                for (int x = 0; x < Math.min(cols, cells.length - startIdx); x++) {
                    String cell = cells[x + startIdx];
                    if (x >= cols || y >= rows) continue; // 防止数组越界
                    
                    JButton button = buttons[y][x];
                    
                    if ("-".equals(cell)) {
                        button.setText("");
                        button.setBackground(null);
                    } else if ("F".equals(cell)) {
                        button.setText("F");
                        button.setBackground(Color.YELLOW);
                    } else if (cell.isEmpty() || " ".equals(cell)) {
                        // 已挖开且周围无地雷
                        button.setText("");
                        button.setBackground(Color.LIGHT_GRAY);
                        button.setEnabled(false);
                    } else {
                        try {
                            // 尝试解析为数字
                            int count = Integer.parseInt(cell);
                            // 已挖开且周围有地雷
                            button.setText(cell);
                            button.setBackground(Color.LIGHT_GRAY);
                            button.setEnabled(false);
                            
                            // 根据周围地雷数量设置不同颜色
                            switch (count) {
                                case 1: button.setForeground(Color.BLUE); break;
                                case 2: button.setForeground(Color.GREEN); break;
                                case 3: button.setForeground(Color.RED); break;
                                case 4: button.setForeground(Color.MAGENTA); break;
                                default: button.setForeground(Color.BLACK); break;
                            }
                        } catch (NumberFormatException e) {
                            // 不是数字，可能是其他特殊符号
                            button.setText(cell);
                            button.setBackground(Color.LIGHT_GRAY);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("更新棋盘失败: " + e.getMessage() + " 数据: " + boardData);
            e.printStackTrace(); // 打印完整堆栈跟踪以便调试
        }
    });
}

    
    /**
     * 主方法
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                String host = "localhost";
                int port = 4444;
                
                // 允许通过命令行参数指定服务器地址和端口
                if (args.length >= 1) {
                    host = args[0];
                }
                if (args.length >= 2) {
                    port = Integer.parseInt(args[1]);
                }
                
                new MinesweeperGUI(host, port);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "启动失败: " + e.getMessage(), 
                        "错误", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}