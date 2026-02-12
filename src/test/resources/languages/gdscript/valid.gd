extends Node2D

var speed = 200
var health = 100

func _ready():
	print("Player is ready")

func _process(delta):
	var velocity = Vector2.ZERO
	if Input.is_action_pressed("ui_right"):
		velocity.x += 1
	if Input.is_action_pressed("ui_left"):
		velocity.x -= 1
	
	position += velocity.normalized() * speed * delta

func take_damage(amount: int):
	health -= amount
	if health <= 0:
		die()

func die():
	queue_free()