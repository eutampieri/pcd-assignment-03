use actix::{Actor, System};
use simulation::{car::Car, message::EnvStep, road::Road, Distance, Identifiable, Instant};

mod simulation;

const N_CARS: usize = 10_000;
const CAR_STARTING_DISTANCE: i64 = 10;

fn main() {
    let system = System::new();

    let cars = (0..N_CARS)
        .into_iter()
        .map(|x| Car::new(simulation::Id::<Car>::from(x)))
        .map(|x| (x.get_id(), system.block_on(async { x.start() })))
        .collect::<Vec<_>>();

    let env = Road::new(cars, Distance::from(1144));
    let env = system.block_on(async { env.start() });

    system.block_on(async {
        for _ in 1..100 {
            println!("New step");
            env.send(EnvStep(Instant::ONE)).await.unwrap();
        }
    });

    system.run().expect("Failed to start simulation");
}
