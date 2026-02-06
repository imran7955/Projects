#include <SFML/Graphics.hpp>
#include<SFML/Audio.hpp>
#include<SFML/Network.hpp>
#include<SFML/Window.hpp>
#include<SFML/System.hpp>
#include<bits/stdc++.h>
using namespace sf;
using namespace std;
int app_width = 1024,app_height = 768;
int road_width = 3300,segLen = 250,camBaseH = 1500,max_displace = road_width*3,speed = 0;
int N = 3600,pos = 0;
float camDepth = 0.85,playerX = 0;;
bool play = 1,over = 0,lossing = 0;
int total_loss = 0;

Music back_music;
Sprite backG,car,paused,finish;
Sprite objects[30],sprites[4000];
ostringstream oss;
Text score_text;
Font font;
Clock clock1,clock2;
//----- textures needed to make sprites ----//
Texture bg,ca,paus_T,fin_t;
Texture tt[30];
void setup_music()
{
    back_music.openFromFile("audio/s.flac");
    back_music.setLoop(true);
    back_music.setVolume(10.0);
}
void setup_canvas()
{
    bg.loadFromFile("images/bg.png");
    ca.loadFromFile("images/car1.png");
    bg.setRepeated(true);
    ca.setSmooth(true);
    backG.setTexture(bg);
    car.setTexture(ca);
    backG.setTextureRect(IntRect(0,0,40000,410));
    backG.setPosition(-5000,0);
    car.setTextureRect(IntRect(0,0,600,414));
}
void setup_pause_img()
{
    paus_T.loadFromFile("images/paused.png");
    paused.setTexture(paus_T);
    paused.setPosition(0,0);
    paused.setTextureRect(IntRect(0,0,1024,768));
    fin_t.loadFromFile("images/ov.png");
    finish.setTexture(fin_t);
    finish.setPosition(app_width/2 - 512,app_height/2-335);
    finish.setTextureRect(IntRect(0,0,1024,700));
}
void load_objects()
{
    for(int i = 1; i <= 8; i++)
    {
        tt[i].loadFromFile("images/" + to_string(i) + ".png");
        tt[i].setSmooth(true);
        objects[i].setTexture(tt[i]);
    }
}
void draw_quad(RenderWindow &w,Color c,int x1,int y1,int w1,int x2,int y2,int w2)
{
    ConvexShape shape(4);
    shape.setFillColor(c);
    shape.setPoint(0,Vector2f(x1-w1,y1));
    shape.setPoint(1,Vector2f(x1+w1,y1));
    shape.setPoint(2,Vector2f(x2+w2,y2));
    shape.setPoint(3,Vector2f(x2-w2,y2));
    w.draw(shape);
}
struct Line
{
    float x,y,z; // co ordinate point of 3d world
    float X,Y,W; // co ordinate point of screen and wide
    float scale,curve,obj_far,clip;
    int sp_index;
    Line(){obj_far = curve = x = y = z = 0;sp_index = -1;}
    void project(int camX,int camY,int camZ)
    {
        scale = camDepth / (z - camZ);
        X = (1 + scale * (x-camX)) * (app_width/2);
        Y = (1 - scale * (y-camY)) * (app_height/2);
        W = scale * road_width * (app_width/2);
    }
    void drawSprite(RenderWindow & app)
    {
        Sprite s = sprites[sp_index];
        int obj_w = s.getTextureRect().width;
        int obj_h = s.getTextureRect().height;
        float obj_x = X + scale * obj_far * app_width/2;
        float obj_y = Y + 4;
        float objW = obj_w * W / 266;
        float objH = obj_h * W / 266;

        obj_x += objW * obj_far; // offset ox X
        obj_y += objH * (-1);

        float clipH = obj_y + objH - clip;
        if(clipH < 0) clipH = 0;
        if(clipH >= objH) return;
        s.setTextureRect(IntRect(0,0,obj_w,obj_h-obj_h*clipH/objH));
        s.setScale(objW/obj_w,objH/obj_h);
        s.setPosition(obj_x,obj_y);
        app.draw(s);
    }
};
vector<Line> construct_lines()
{
    vector<Line> lines;
    for(int i = 0; i < 3600; i++)
    {
        Line temp_line;
        temp_line.z = i * segLen;
        if(i < 800 && i %17 == 0) {temp_line.obj_far = -1.5; sprites[i] = objects[1]; temp_line.sp_index = i;}
        if(i > 850 && i < 1350 && i % 29== 0) {temp_line.obj_far = -1.5; sprites[i] = objects[4]; temp_line.sp_index = i;}
        if(i < 450 && i %19== 0) {temp_line.obj_far = 1.8; sprites[i] = objects[2]; temp_line.sp_index = i;}
        if(i > 450 && i < 850 && i %39== 0) {temp_line.obj_far = 1.5; sprites[i] = objects[3]; temp_line.sp_index = i;}
        if(i > 850 && i < 1350 && i % 27== 0) {temp_line.obj_far = 1.5; sprites[i] = objects[5]; temp_line.sp_index = i;}
        if(i > 1350 && i < 1550&& i % 27== 0) {temp_line.obj_far = -2.1; sprites[i] = objects[7]; temp_line.sp_index = i;}
        if(i > 1350 && i % 23== 0) {temp_line.obj_far = 1.5; sprites[i] = objects[6]; temp_line.sp_index = i;}
        if(i > 1600 && i < 2600 && i % 29== 0) {temp_line.obj_far = -1.5; sprites[i] = objects[4]; temp_line.sp_index = i;}
        if(i == 842 || i == 1570) {temp_line.obj_far = -1.5; sprites[i] = objects[8]; temp_line.sp_index = i;}
        if(i > 188 && i < 566) temp_line.y = sin(i/30.0) * camBaseH*2;
        if(i > 800 && i < 1600) temp_line.curve = .9;
        if(i > 2600 && i < 3500) temp_line.curve = -.9;
        lines.push_back(temp_line);
    }
    return lines;
}
void score_setup()
{
    font.loadFromFile("COMIC.TTF");
    score_text.setFont(font);
    score_text.setColor(Color::Black);
    score_text.setCharacterSize(30);
    score_text.setPosition(10,10);
    score_text.setString(oss.str());
}

void listen_keyboard()
{
    if(Keyboard::isKeyPressed(Keyboard::Up)) {speed =250; }
    if(Keyboard::isKeyPressed(Keyboard::Down)) {speed = 0; }
    if(Keyboard::isKeyPressed(Keyboard::Left)) playerX = max((float)-max_displace,playerX-50);
    if(Keyboard::isKeyPressed(Keyboard::Right)) playerX = min((float)max_displace,playerX+50);
    if(Keyboard::isKeyPressed((Keyboard::W))) camBaseH = min(7500,camBaseH+80);
    if(Keyboard::isKeyPressed((Keyboard::S))) camBaseH = max(500,camBaseH-80);
    if(Keyboard::isKeyPressed((Keyboard::Add))) road_width = min(5000,road_width+80);
    if(Keyboard::isKeyPressed((Keyboard::Subtract))) road_width = max(2000,road_width-80);
    if(Keyboard::isKeyPressed((Keyboard::N))) back_music.setVolume(max(0.f,back_music.getVolume() - 1));
    if(Keyboard::isKeyPressed((Keyboard::M))) back_music.setVolume(min(100.f,back_music.getVolume() + 1));
    if(Keyboard::isKeyPressed((Keyboard::X))) play = 0;
    if(Keyboard::isKeyPressed((Keyboard::Z)))
    {
        if(!play)
        {
            total_loss += clock2.getElapsedTime().asSeconds();
            play = 1,lossing = 0;
            back_music.play();
        }
        else play = 1;
    }
}
void display_score(RenderWindow &app,int time_s)
{
    oss.str("");
    oss << "Time: " << setprecision(3) << time_s/60 << " m " << time_s%60 << " s " << " | Distance " << pos/(250*1000.0) << " km";
    score_text.setString(oss.str());
    app.draw(score_text);
}
void set_car_position()
{
    float scf = 1 - camBaseH / (camBaseH + 1000.0);
    car.setScale(scf,scf);
    float carX = app_width/2 - car.getTextureRect().width/2*scf;
    float carY = app_height - car.getTextureRect().height*scf;
    car.setPosition(carX,carY);
}

int main()
{
    RenderWindow app(VideoMode(app_width, app_height), "Racing Hub");
    app.setFramerateLimit(60);

    load_objects();
    vector<Line> lines = construct_lines();
    setup_canvas();
    setup_pause_img();
    setup_music();
    back_music.play();
    score_setup();

    clock1.restart();
    while (app.isOpen())
    {
        Event event;
        while (app.pollEvent(event))
        {
            if (event.type == Event::Closed)
                app.close();
        }
        listen_keyboard();
        if(!play)
        {
            if(!lossing) {clock2.restart(); lossing = 1; back_music.pause();}
            app.draw(paused);
            app.display();
            continue;
        }
        if(pos >= 3597*250 || over)
        {
            pos = speed = total_loss = 0;
            //over = 0;
            app.draw(finish);
            app.display();
            over = 1;
            if(Keyboard::isKeyPressed(Keyboard::Z)) {over = lossing = 0;  clock1.restart(); back_music.play();}
            continue;
        }
        app.clear(Color(51, 153, 51));
        app.draw(backG);
        pos += speed;
        while(pos >= N*segLen) pos -= N*segLen;
        while(pos < 0) pos += N*segLen;
        int startPos = pos / segLen;
        float curve = 0,curve_inc = 0;
        int camH = camBaseH + lines[startPos].y;
        int maxY = app_height;
        if(speed > 0) backG.move(-lines[startPos].curve*2.0,0);
        if(speed < 0) backG.move(lines[startPos].curve*2.0,0);
        set_car_position();
        for(int i = startPos; i < startPos+300; i++)
        {
            Line &next = lines[i%N];
            Line prev = lines[(i-1)%N];

            // in terms of y axis, computer is opposite of cartesian plane.
            // So a line which appears next/far in "lines" actually printed near from user end
            next.project(playerX - curve,camH,pos - (i>=N?N*segLen:0));

            curve += curve_inc;
            curve_inc += next.curve;
            if(i == startPos && speed != 0) playerX += (-speed)/abs(speed) * 50*next.curve;

            next.clip = maxY;
            if(next.Y >= maxY) continue;
            maxY = next.Y;

            Color road = (i/3)%2?Color(60,60,60):Color(64,64,64);
            Color grass = (i/3)%2?Color(0,153,51):Color(0,210,4);
            Color rumble = (i/3)%2?Color(255,0,0):Color(255,255,255);

            if(i >= 3580 && i < 3600) road = Color::White;

            draw_quad(app,grass,0,prev.Y,app_width,0,next.Y,app_width);
            draw_quad(app,rumble,prev.X,prev.Y,prev.W*1.1,next.X,next.Y,next.W*1.1);
            draw_quad(app,road,prev.X,prev.Y,prev.W,next.X,next.Y,next.W);
            if(((i+3)/3)%2 == 0)
                draw_quad(app,Color(255,255,255),prev.X,prev.Y,prev.W*0.02,next.X,next.Y,next.W*0.02);
        }
        for(int j = startPos+300; j > startPos; j--)
           if(lines[j%N].sp_index >= 0) lines[j%N].drawSprite(app);
        app.draw(car);
        display_score(app,clock1.getElapsedTime().asSeconds()-total_loss);
        app.display();
    }
    return 0;
}
