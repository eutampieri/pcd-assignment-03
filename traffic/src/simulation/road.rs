use std::{future::IntoFuture, task::Context};

use actix::{Actor, ActorFutureExt, Addr, AsyncContext, Context as ActixContext, Handler};
use num_traits::ConstZero;
use road::message::Step;

use self::message::EnvStep;

use super::*;
use rayon::prelude::*;

#[derive(Debug, Default)]
pub struct Road {
    id: Id<Self>,
    length: super::Distance,
    car_positions: std::collections::HashMap<Id<car::Car>, super::Distance>,
    cars: Vec<(Id<car::Car>, Addr<car::Car>)>,
    counter: usize,
}

impl Road {
    pub fn new(cars: Vec<(Id<car::Car>, Addr<car::Car>)>, length: Distance) -> Self {
        Self {
            car_positions: cars
                .iter()
                .enumerate()
                .map(|(i, (x, _))| (x.clone(), Distance::from(i as i64 * 10)))
                .collect(),
            cars,
            length,
            ..Default::default()
        }
    }
    fn get_car_in_front_of<'a>(&'a self, car: Id<car::Car>) -> Option<&'a Id<car::Car>> {
        let position = self.get_car_position(&car)?;
        self.car_positions
            .par_iter()
            .map(|(i, d)| (i, (*d - *position).rem_euclid(self.length)))
            .filter(|(_, d)| *d > Distance::ZERO)
            .min_by(|x, y| x.1.cmp(&y.1))
            .map(|x| x.0)
    }

    fn get_car_position<'a>(&'a self, car: &Id<car::Car>) -> Option<&'a Distance> {
        self.car_positions.get(car)
    }
}

impl Identifiable for Road {
    fn get_id(&self) -> super::Id<Self> {
        self.id.clone()
    }
}

impl Actor for Road {
    type Context = ActixContext<Self>;

    fn started(&mut self, _ctx: &mut Self::Context) {
        println!("Road {:?} started!", self.id);
        //System::current().stop(); // <- stop system
    }
}

impl Handler<EnvStep> for Road {
    type Result = ();

    fn handle(&mut self, msg: EnvStep, ctx: &mut Self::Context) -> Self::Result {
        println!("Starting handler {}", self.counter);
        let futs = self
            .cars
            .iter()
            .map(|car| {
                let distance = self
                    .get_car_in_front_of(car.0.clone())
                    .and_then(|x| self.get_car_position(x))
                    .cloned();
                //car.1.do_send(Step(msg.0, distance));
                ctx.spawn(
                    actix::fut::wrap_future::<_, Self>(
                        car.1.send(Step(msg.0, distance)).into_future(),
                    )
                    .map(|_, _, _| ()),
                )
            })
            .collect::<Vec<_>>();
        println!("Handler done {}!", self.counter);
        self.counter += 1;
    }
}
