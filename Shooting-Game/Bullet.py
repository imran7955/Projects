import Globals
from Globals import *
import pygame
import os
bullet_image = pygame.image.load("images/asset/bullet.png")
class bullet(pygame.sprite.Sprite):
    def __init__(self, x, y,direction):
            pygame.sprite.Sprite.__init__(self)
            self.x = x
            self.y = y
            self.speed = 35
            self.direction = direction
            self.rect = bullet_image.get_rect()
    def update(self,sol):
            if pygame.sprite.spritecollide(sol, pygame.sprite.Group([self]), False):
                if sol.alive:
                    if self in bul_arr: 
                        bul_arr.remove(self)
    def draw_bullet(self,screen_name):
        bul_width = int(bullet_image.get_width())
        bul_height = int(bullet_image.get_height())
        scale = 1
        scaled_image = pygame.transform.scale(bullet_image,(bul_width*scale,bul_height*scale))
        self.rect = scaled_image.get_rect()
        self.rect.center = (self.x,self.y)
        screen_name.blit(scaled_image,self.rect)
    def move(self):
        self.x += self.speed * self.direction + Globals.screen_scroll
        if self.rect.left > screen_width or self.rect.right < 0:
            bul_arr.remove(self)
