package Lab;

import java.awt.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel {
    private Double[][] graphicsData;
    private List<Double[]> extrems;
    private List<Double[]> globExtrems;
    // Флаговые переменные, задающие правила отображения графика
    private boolean showAxis = true;
    private boolean showMarkers = true;
    private boolean showExtrem = true;
    // Границы диапазона пространства, подлежащего отображению
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;




    // Используемый масштаб отображения
    private double scale;
    // Различные стили черчения линий
    private BasicStroke graphicsStroke;
    private BasicStroke axisStroke;
    private BasicStroke markerStroke;
    // Различные шрифты отображения надписей
    private Font axisFont;


    private Point2D.Double selectionStart; // Начальная точка выделения
    private Rectangle2D.Double selectionRect; // Текущая область выделения
    private boolean isSelecting = false; // Флаг выделения





    private int mouseX = -1; // Координаты мыши X
    private int mouseY = -1; // Координаты мыши Y







    public GraphicsDisplay() {
        setBackground(Color.WHITE);
        graphicsStroke = new BasicStroke(3f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND, 10.0f,
                new float[]{8, 2, 4, 2, 2, 2, 4, 2, 8}, 1.0f);
        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        axisFont = new Font("Serif", Font.BOLD, 36);


        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                repaint();
            }
        });


        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) { // Левая кнопка мыши
                    selectionStart = new Point2D.Double(e.getX(), e.getY());
                    isSelecting = true;
                    selectionRect = null;
                }
                if (e.getButton() == MouseEvent.BUTTON3) { // Правая кнопка мыши
                    resetZoom();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isSelecting && selectionRect != null) {
                    zoomToSelection();
                }
                isSelecting = false;
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isSelecting) {
                    updateSelectionRect(e.getX(), e.getY());
                    repaint();
                }
            }
        });





    }
    public void showGraphics(Double[][] graphicsData) {


        this.graphicsData = graphicsData;
        repaint();
        extrems = findLocalExtrema(graphicsData);
        globExtrems=findExtrema(graphicsData);

        minX = graphicsData[0][0];
        maxX = graphicsData[graphicsData.length - 1][0];
        minY = graphicsData[0][1];
        maxY = minY;

        for (int i = 1; i < graphicsData.length; i++) {
            if (graphicsData[i][1] < minY) {
                minY = graphicsData[i][1];
            }
            if (graphicsData[i][1] > maxY) {
                maxY = graphicsData[i][1];
            }
        }
    }

    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }
    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }
    public void setShowExtrem(boolean showExtrem) {
        this.showExtrem = showExtrem;
        repaint();
    }
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graphicsData == null || graphicsData.length == 0) return;
        Graphics2D canvas = (Graphics2D) g;
        drawMouseCoordinates(canvas);

        /*if (minY > 0) {
            minY = -1;
        }
        if (maxY < 0) {
            maxY = 1;
        }*/

        int panelWidth = getSize().width;
        int panelHeight = getSize().height;

        double scaleX = (double) getSize().width / (maxX - minX);
        double scaleY = (double) getSize().height / (maxY - minY);
        scale = Math.min(scaleX, scaleY);

        double yIncrement = (panelHeight / scale - (maxY - minY)) / 2;
        double xIncrement = (panelWidth / scale - (maxX - minX)) / 2;

        maxX += xIncrement;
        minX -= xIncrement;
        maxY += yIncrement;
        minY -= yIncrement;

        if (selectionRect != null) {
            canvas.setColor(Color.GRAY);
            canvas.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10.0f, new float[]{10, 5}, 0.0f));
            canvas.draw(selectionRect);
        }




        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();

        if (showAxis) paintAxis(canvas);
        paintGraphics(canvas);
        if (showMarkers) paintMarkers(canvas);
        if (showExtrem) paintExtrem(canvas);

        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);


    }
    protected void paintGraphics(Graphics2D canvas) {
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        canvas.setStroke(graphicsStroke);
        canvas.setColor(Color.BLACK);
        GeneralPath graphics = new GeneralPath();
        for (int i = 0; i < graphicsData.length; i++) {
            Point2D.Double point = xyToPoint(graphicsData[i][0],
                    graphicsData[i][1]);
            if (i > 0) {
                graphics.lineTo(point.getX(), point.getY());
            } else {
                graphics.moveTo(point.getX(), point.getY());
            }
        }
        canvas.draw(graphics);
    }
    protected void paintExtrem(Graphics2D canvas) {
        canvas.setStroke(markerStroke);
        for (Double[] point : graphicsData) {
            boolean isExtrem = false;
            boolean isGlobExtrem=false;
            // Цикл для проверки точек из массива graphicsData в списке extrems
            for (Double[] extrem : extrems) {
                if (point[0].equals(extrem[0]) && point[1].equals(extrem[1])) {
                    isExtrem = true;
                    break;
                }
            }
            for (Double[] extrem : globExtrems) {
                if (point[0].equals(extrem[0]) && point[1].equals(extrem[1])) {
                    isGlobExtrem = true;
                    break;
                }
            }


            if (isExtrem) {

                canvas.setColor(Color.GREEN);
                Point2D.Double center = xyToPoint(point[0], point[1]);
                double size = 100; // Размер маркера
                drawCircle(canvas, center, size); // Метод для рисования кружка
                drawValue(canvas, center, point[1]); // Метод для вывода значения экстремума
            }
            if (isGlobExtrem) {

                canvas.setColor(Color.RED);
                Point2D.Double center = xyToPoint(point[0], point[1]);
                double size = 100; // Размер маркера
                drawCircle(canvas, center, size); // Метод для рисования кружка
                drawValue(canvas, center, point[1]); // Метод для вывода значения экстремума
            }

        }
    }
    private void drawCircle(Graphics2D canvas, Point2D.Double center, double size) {
        double halfSize = size / 2;
        canvas.draw(new Ellipse2D.Double(center.x - halfSize, center.y - halfSize, size, size));
    }
    private void drawValue(Graphics2D canvas, Point2D.Double center, double value) {
        canvas.drawString(String.valueOf(value), (float) center.x + 5, (float) center.y - 5);
    }
    protected void paintMarkers(Graphics2D canvas) {
        canvas.setStroke(markerStroke);

        // Вычисление среднего значения функции


        for (Double[] point : graphicsData) {
            Color markerColor;
            // Условие для изменения цвета маркера
            boolean isEven = true;
            double number= point[1];
                    number = Math.abs(number); // Обрабатываем отрицательные числа
                    while (number > 0) {
                        int digit = (int) number % 10; // Извлекаем последнюю цифру
                        if (digit % 2 != 0) { // Проверяем, является ли цифра нечетной
                            isEven=false;
                            break;
                        }
                        number /= 10; // Убираем последнюю цифру
                    }





            if (isEven) {
                markerColor = Color.RED;
                //System.out.println("Значение " + point[1] + " в точке (" + point[0] + ") содержит исключительно чётные числа в целой части: ");
            } else {
                markerColor = Color.BLACK; // Цвет по умолчанию
            }



            canvas.setColor(markerColor);




            Point2D.Double center = xyToPoint(point[0], point[1]);
            double size = 25; // Размер маркера


            drawDiamond(canvas, center, size);
        }
    }
    private void drawDiamond(Graphics2D canvas, Point2D.Double center, double size) {
        double halfSize = size / 2;
        double x = center.x;
        double y = center.y;

        Point2D.Double top = new Point2D.Double(x, y - halfSize);
        Point2D.Double right = new Point2D.Double(x + halfSize, y);
        Point2D.Double bottom = new Point2D.Double(x, y + halfSize);
        Point2D.Double left = new Point2D.Double(x - halfSize, y);


        canvas.draw(new Line2D.Double(top, right));
        canvas.draw(new Line2D.Double(right, bottom));
        canvas.draw(new Line2D.Double(bottom, left));
        canvas.draw(new Line2D.Double(left, top));
    }
    protected void paintAxis(Graphics2D canvas) {
        // Установить особое начертание для осей
        canvas.setStroke(axisStroke);
        // Оси рисуются черным цветом
        canvas.setColor(Color.BLACK);
        // Стрелки заливаются черным цветом
        canvas.setPaint(Color.BLACK);
        // Подписи к координатным осям делаются специальным шрифтом
        canvas.setFont(axisFont);
        // Создать объект контекста отображения текста - для получения характеристик устройства (экрана)
        FontRenderContext context = canvas.getFontRenderContext();

        // Определить, должна ли быть видна ось Y на графике
        if (minX <= 0.0 && maxX >= 0.0) {
            // Рисуем ось Y
            canvas.draw(new Line2D.Double(xyToPoint(0, maxY), xyToPoint(0, minY)));

            // Рисуем стрелку оси Y
            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(0, maxY);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX() + 5, arrow.getCurrentPoint().getY() + 20);
            arrow.lineTo(arrow.getCurrentPoint().getX() - 10, arrow.getCurrentPoint().getY());
            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);

            // Подпись к оси Y
            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0, maxY);
            canvas.drawString("y", (float) labelPos.getX() + 10, (float) (labelPos.getY() - bounds.getY()));
        }

        // Определить, должна ли быть видна ось X на графике
        if (minY <= 0.0 && maxY >= 0.0) {
            // Рисуем ось X
            canvas.draw(new Line2D.Double(xyToPoint(minX, 0), xyToPoint(maxX, 0)));

            // Рисуем стрелку оси X
            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(maxX, 0);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX() - 20, arrow.getCurrentPoint().getY() - 5);
            arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY() + 10);
            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);

            // Подпись к оси X
            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = xyToPoint(maxX, 0);
            canvas.drawString("x", (float) (labelPos.getX() - bounds.getWidth() - 10), (float) (labelPos.getY() + bounds.getY()));
        }

        // Рисуем ноль на пересечении
        Point2D.Double origin = xyToPoint(0, 0);
        canvas.drawString("0", (float) origin.getX() + 0, (float) origin.getY() + 4); // Смещение для лучшей видимости
    }
    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - minX;
        double deltaY = maxY - y;
        return new Point2D.Double(deltaX * scale *0.9, deltaY * scale* 0.9 ); // Уменьшение размера
    }
    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
        Point2D.Double dest = new Point2D.Double();
        dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
        return dest;
    }

    public static List<Double[]> findLocalExtrema(Double[][] points) {
            List<Double[]> extrema = new ArrayList<>();

            if (points == null || points.length < 3) {
                return extrema; // Нужно минимум 3 точки для нахождения экстремумов
            }

            for (int i = 1; i < points.length - 1; i++) {
                double yPrev = points[i-1][1];
                double yCurrent = points[i][1];
                double yNext = points[i+1][1];


                if ((yCurrent > yPrev && yCurrent > yNext) || (yCurrent < yPrev && yCurrent < yNext)) {
                    extrema.add(points[i]);
                    System.out.println(points[i][1]);
                }

            }

            return extrema;
        }
    public static List<Double[]> findExtrema(Double[][] points) {
        List<Double[]> extrema = new ArrayList<>(2);

        if (points == null) {
            return extrema;
        }
        double minX=points[0][0];
        double maxX=minX;
        double minY=points[0][1];
        double maxY=minY;
        int i1=0;
        int i2=0;

        for (int i = 1; i < points.length; i++) {

            if(minX>points[i][0]) {
                minX = points[i][0];
                minY = points[i][1];
                i1=i;
            }
            if(maxX<points[i][0]) {
                maxX = points[i][0];
                maxY=points[i][1];
                i2=i;
            }

        }
        extrema.add(points[i1]);
        extrema.add(points[i2]);
        System.out.println(points[i1][1]);
        System.out.println(points[i2][1]);
        return extrema;
    }




    // фигня для мыши

    private double lastValidX = -1; // Последняя корректная X-координата
    private double lastValidY = -1; // Последняя корректная Y-координата

    private void updateSelectionRect(double x, double y) {
        double startX = selectionStart.getX();
        double startY = selectionStart.getY();

        // Проверяем, удовлетворяют ли текущие координаты условию
        if (x > startX ) {
            lastValidX = x;
        }
        if(y > startY) {
            lastValidY = y;
        }
        // Если нет корректных значений, использовать начальную точку
        double endX = (lastValidX > startX) ? lastValidX : startX;
        double endY = (lastValidY > startY) ? lastValidY : startY;

        // Создаем прямоугольник на основе последних корректных координат
        double width = endX - startX;
        double height = endY - startY;

        selectionRect = new Rectangle2D.Double(startX, startY, width, height);
    }

    private void zoomToSelection() {
        if (selectionRect == null) return;

        // Конвертируем координаты из экранных в логические
        Point2D.Double p1 = pointToXY(selectionRect.x, selectionRect.y);
        Point2D.Double p2 = pointToXY(selectionRect.x + selectionRect.width, selectionRect.y + selectionRect.height);

        // Устанавливаем новые границы
        /*minX = Math.min(p1.x, p2.x);
        maxX = Math.max(p1.x, p2.x);
        minY = Math.min(p1.y, p2.y);
        maxY = Math.max(p1.y, p2.y);*/
        minX = p1.x;
        maxX = p2.x;
        minY = p2.y;
        maxY = p1.y;


        selectionRect = null;
        repaint();
    }
    private void resetZoom() {
        showGraphics(graphicsData); // Просто пересчитываем исходные границы
    }
    private Point2D.Double pointToXY(double x, double y) {
        double realX = minX + x / (scale );
        double realY = maxY - y / (scale );
        return new Point2D.Double(realX, realY);
    }



    private void drawMouseCoordinates(Graphics2D canvas) {
        if (mouseX < 0 || mouseY < 0) return; // Проверка на корректность координат

        Double[] closestPoint = findClosestPoint(mouseX, mouseY);
        if (closestPoint != null) {
            Point2D.Double screenPoint = xyToPoint(closestPoint[0], closestPoint[1]);
            canvas.setColor(Color.BLUE);
            canvas.drawString(String.format("(%.2f, %.2f)", closestPoint[0], closestPoint[1]),
                    (float) screenPoint.x + 10, (float) screenPoint.y - 10);
        }
    }
    private Double[] findClosestPoint(int mouseX, int mouseY) {
        Double[] closestPoint = null;
        double minDistance = Double.MAX_VALUE;

        for (Double[] point : graphicsData) {
            Point2D.Double screenPoint = xyToPoint(point[0], point[1]);
            double distance = Math.hypot(screenPoint.x - mouseX, screenPoint.y - mouseY);

            if (distance < minDistance) {
                minDistance = distance;
                closestPoint = point;
            }
        }

        // Условие: если мышь находится слишком далеко от точки, не возвращаем её
        if (minDistance > 10) { // Задаём "пороговое расстояние" для реакции
            return null;
        }
        return closestPoint;
    }
    void veryImpForResize(){

    }
}

