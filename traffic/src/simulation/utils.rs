use actix::dev::MessageResponse;
use actix::Actor;
use actix::Message;
use num_traits::ConstZero;
use num_traits::One;
use num_traits::Zero;

use std::ops::Add;

use std::ops::Sub;

use std::marker::PhantomData;

pub trait Identifiable: Sized {
    fn get_id(&self) -> Id<Self>;
}

/// A strongly typed object identifier.
#[derive(PartialOrd, Copy, Default)]
pub struct Id<T>(usize, std::marker::PhantomData<T>);

impl<T> From<usize> for Id<T> {
    fn from(value: usize) -> Self {
        Self(value, PhantomData)
    }
}

impl<T> Clone for Id<T> {
    fn clone(&self) -> Self {
        Self(self.0.clone(), self.1.clone())
    }
}

impl<T> std::fmt::Debug for Id<T> {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.debug_tuple(&("Id<".to_owned() + std::any::type_name::<T>() + ">"))
            .field(&self.0)
            .finish()
    }
}

impl<T> PartialEq for Id<T> {
    fn eq(&self, other: &Self) -> bool {
        self.0 == other.0
    }
}

impl<T> Eq for Id<T> {}

impl<T> std::hash::Hash for Id<T> {
    fn hash<H: std::hash::Hasher>(&self, state: &mut H) {
        self.0.hash(state);
    }
}

#[derive(PartialEq, PartialOrd, Clone, Copy, Debug, Default, Eq, Ord)]
/// A strongly typed pixel size.
pub struct Distance(i64);

impl From<i64> for Distance {
    fn from(value: i64) -> Self {
        Self(value)
    }
}

impl Sub for Distance {
    type Output = Distance;

    fn sub(self, rhs: Self) -> Self::Output {
        Self(self.0 - rhs.0)
    }
}

impl Add for Distance {
    type Output = Distance;

    fn add(self, rhs: Self) -> Self::Output {
        Self(self.0 + rhs.0)
    }
}

impl Zero for Distance {
    fn zero() -> Self {
        Self(0)
    }

    fn is_zero(&self) -> bool {
        self.0 == 0
    }
}

impl ConstZero for Distance {
    const ZERO: Self = Self(0);
}

impl Distance {
    pub fn rem_euclid(&self, rhs: Self) -> Self {
        Self(self.0.rem_euclid(rhs.0))
    }
}

#[derive(Debug, Clone, Copy)]
pub struct Instant(u64);
impl Instant {
    pub const ONE: Self = Self(1);
}
#[derive(Debug, Default)]
pub struct Speed(u8);

impl Speed {
    pub fn inner(&self) -> u8 {
        self.0
    }

    pub fn accellerate(&mut self, max: Self) {
        self.0 = (self.0 * 2).clamp(0, max.0)
    }

    pub fn brake(&mut self) {
        self.0 = 0
    }
}

impl From<u8> for Speed {
    fn from(value: u8) -> Self {
        Self(value)
    }
}

impl std::ops::Mul<Instant> for Speed {
    type Output = Distance;

    fn mul(self, rhs: Instant) -> Self::Output {
        Distance((self.0 as u64 * rhs.0).try_into().unwrap())
    }
}
